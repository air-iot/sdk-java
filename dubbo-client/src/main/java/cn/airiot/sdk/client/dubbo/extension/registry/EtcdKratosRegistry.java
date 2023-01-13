package cn.airiot.sdk.client.dubbo.extension.registry;

import cn.airiot.sdk.client.dubbo.extension.utils.DubboUtils;
import com.google.gson.Gson;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EtcdKratosRegistry implements Registry {

    private final Logger log = LoggerFactory.getLogger(EtcdKratosRegistry.class);

    private final URL url;
    private final Client etcdClient;

    private final Map<String, ServiceSubscriber> subscribers = new ConcurrentHashMap<>();

    public EtcdKratosRegistry(URL url) {
        this.url = url;
        this.etcdClient = Client.builder()
                .endpoints(String.format("grpc://%s:%d", url.getHost(), url.getPort()))
                .user(ByteSequence.from(url.getUsername().getBytes(StandardCharsets.UTF_8)))
                .password(ByteSequence.from(url.getPassword().getBytes(StandardCharsets.UTF_8)))
                .keepaliveTime(Duration.ofSeconds(15))
                .keepaliveTimeout(Duration.ofSeconds(30))
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
        if (this.subscribers.isEmpty()) {
            return;
        }

        for (ServiceSubscriber subscriber : this.subscribers.values()) {
            subscriber.unsubscribe();
        }

        this.subscribers.clear();
    }

    @Override
    public void register(URL url) {
    }

    @Override
    public void unregister(URL url) {
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        log.info("订阅服务: {}", url);

        String service = DubboUtils.getServiceName(url);
        String PREFIX = "/microservices/";
        String servicePrefix = PREFIX + service + "/";
        ServiceSubscriber subscriber = new ServiceSubscriber(
                service, this.etcdClient, servicePrefix, listener
        );

        subscriber.subscribe();
        this.subscribers.putIfAbsent(service, subscriber);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        log.info("取消服务订阅: {}", url);
        String service = DubboUtils.getServiceName(url);

        if (!this.subscribers.containsKey(service)) {
            return;
        }

        ServiceSubscriber subscriber = this.subscribers.get(service);
        subscriber.unsubscribe();
        this.subscribers.remove(service);
    }

    @Override
    public List<URL> lookup(URL url) {
        System.out.println("lookup:" + url);
        return null;
    }


    static class ServiceSubscriber implements Watch.Listener {
        private final Logger log;
        private final String service;
        private final Client client;
        private final String prefix;
        private final NotifyListener listener;
        private Watch.Watcher watcher;

        private final Gson gson = new Gson();
        /**
         * 服务实例列表
         */
        private final List<URL> instances = new ArrayList<>();

        protected ServiceSubscriber(String service, Client client, String prefix, NotifyListener listener) {
            this.log = LoggerFactory.getLogger(String.format("Registry[%s]", service));
            this.service = service;
            this.client = client;
            this.prefix = prefix;
            this.listener = listener;
        }

        /**
         * 同步一次数制
         */
        public void sync() {
            log.debug("订阅服务: 获取最新服务实例列表");
            try {
                GetResponse response = this.client.getKVClient()
                        .get(ByteSequence.from(this.prefix.getBytes(StandardCharsets.UTF_8)),
                                GetOption.newBuilder().isPrefix(true).build()
                        )
                        .get(15, TimeUnit.SECONDS);
                List<KeyValue> instances = response.getKvs();
                if (CollectionUtils.isEmpty(instances)) {
                    throw new IllegalStateException("订阅服务: 未查询到服务实例, service = " + service);
                }

                List<URL> serviceUrls = new ArrayList<>(instances.size());
                for (KeyValue instance : instances) {
                    if (log.isTraceEnabled()) {
                        log.trace("订阅服务: 服务实例信息, {}", instance.getValue().toString(StandardCharsets.UTF_8));
                    }

                    Optional<URL> inst = this.buildInstance(instance);
                    if (!inst.isPresent()) {
                        continue;
                    }

                    serviceUrls.add(inst.get());
                }

                if (serviceUrls.isEmpty()) {
                    log.warn("订阅服务: 所有服务实例均未提供 grpc 服务, service = {}, instances = {}", service, instances);
                }

                this.instances.addAll(serviceUrls);
                listener.notify(serviceUrls);
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException | TimeoutException e) {
                throw new IllegalStateException("订阅服务: 查询服务信息失败, prefix = " + prefix, e);
            }
        }

        public void subscribe() {
            this.sync();
            this.watcher = this.client.getWatchClient().watch(
                    ByteSequence.from(this.prefix, StandardCharsets.UTF_8),
                    WatchOption.newBuilder().isPrefix(true).build(),
                    this
            );
        }

        @Override
        public synchronized void onNext(WatchResponse response) {
            List<WatchEvent> events = response.getEvents();
            for (WatchEvent event : events) {
                switch (event.getEventType()) {
                    case PUT:
                        this.addInstance(event.getKeyValue());
                        break;
                    case DELETE:
                        this.deleteInstance(event.getKeyValue());
                    default:
                        if (log.isDebugEnabled()) {
                            log.debug("订阅服务: 服务实例信息发生未知变化, {}", event.getKeyValue().getValue().toString(StandardCharsets.UTF_8));
                        }
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("订阅服务: 监听 {} 失败", this.prefix, throwable);
        }

        @Override
        public void onCompleted() {

        }

        public void unsubscribe() {
            if (this.watcher != null) {
                this.watcher.close();
            }
        }

        /**
         * 新的服务实例上线
         */
        private void addInstance(KeyValue keyValue) {
            if (log.isTraceEnabled()) {
                log.trace("新服务实例上线: 当前服务实例列表, {}", this.instances);
            }
            if (log.isDebugEnabled()) {
                log.debug("新服务实例上线: {}", keyValue.getValue().toString(StandardCharsets.UTF_8));
            }
            Optional<URL> inst = this.buildInstance(keyValue);
            if (!inst.isPresent()) {
                return;
            }

            this.instances.add(inst.get());

            if (log.isTraceEnabled()) {
                log.trace("新服务实例上线: 更新后服务实例列表, {}", this.instances);
            }

            this.listener.notify(this.instances);
        }

        /**
         * 已有服务实例下线
         */
        public void deleteInstance(KeyValue keyValue) {
            if (log.isTraceEnabled()) {
                log.trace("服务实例下线: 当前服务实例列表, {}", this.instances);
            }
            if (log.isDebugEnabled()) {
                log.debug("服务实例下线: {}", keyValue.getKey().toString(StandardCharsets.UTF_8));
            }

            String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
            String removedInstId = key.substring(this.prefix.length()).trim();
            
            boolean changed = false;
            Iterator<URL> it = this.instances.iterator();
            while (it.hasNext()) {
                URL inst = it.next();
                String instId = inst.getAttribute(ServiceConstants.SERVICE_INSTANCE_ID).toString();
                if (removedInstId.equals(instId)) {
                    it.remove();
                    changed = true;
                    break;
                }
            }

            if (log.isTraceEnabled()) {
                log.trace("服务实例下线: 更新后服务实例列表, {}", this.instances);
            }

            if (!changed) {
                log.debug("服务实例下线: 服务实例列表无变化, {}", this.instances);
                return;
            }

            this.listener.notify(this.instances);
        }

        private Optional<URL> buildInstance(KeyValue keyValue) {
            ServiceInstance inst = gson.fromJson(keyValue.getValue().toString(StandardCharsets.UTF_8), ServiceInstance.class);
            Optional<String> endpoint = inst.getEndpoints().stream().filter(ep -> ep.startsWith("grpc://")).findAny();
            if (!endpoint.isPresent()) {
                log.warn("构建服务实例: 实例未提供 grpc 服务, service = {}, instance = {}", service, inst);
                return Optional.empty();
            }

            if (!StringUtils.hasText(inst.getId())) {
                log.warn("构建服务实例: 服务实例ID为空");
                return Optional.empty();
            }

            URL url = URL.valueOf(endpoint.get());
            url = url.putAttribute(ServiceConstants.SERVICE_INSTANCE_ID, inst.getId().trim());
            url = url.putAttribute(ServiceConstants.METADATA_KEY, inst.getMetadata());
            url = url.putAttribute(ServiceConstants.VERSION_KEY, inst.getVersion());
            return Optional.of(url);
        }
    }
}
