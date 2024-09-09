package io.github.airiot.config.etcd;

import com.google.gson.Gson;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class EtcdConfigWatch implements ApplicationEventPublisherAware, SmartLifecycle {

    private final Logger logger = LoggerFactory.getLogger(EtcdConfigWatch.class);

    private final Gson gson = new Gson();

    private final Client client;
    private final EtcdConfigProperties properties;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ApplicationEventPublisher applicationEventPublisher;

    private KV kvClient;
    private Watch watchClient;
    private Watch.Watcher watcher;
    private Thread watchThread;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) throws BeansException {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public EtcdConfigWatch(Client client, EtcdConfigProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void start() {
        if (!this.running.compareAndSet(false, true)) {
            return;
        }

        this.kvClient = this.client.getKVClient();

        if (this.watchThread != null) {
            this.watchThread.interrupt();
        }

        this.watchThread = new Thread(this::watchTask);
        this.watchThread.setName("etcd-config-watch");
        this.watchThread.setDaemon(true);
        this.watchThread.start();
    }

    private void watchTask() {
        String configKey = this.properties.getConfigKey();
        long intervalMs = this.properties.getCheckInterval().toMillis();
        long readTimeoutMs = this.properties.getReadTimeout().toMillis();

        ByteSequence key = ByteSequence.from(this.properties.getConfigKey(), StandardCharsets.UTF_8);
        AiriotConfig lastConfig = null;

        logger.info("监听平台配置: {}", configKey);

        while (this.running.get()) {
            logger.debug("监听平台配置: {}", configKey);
            try {
                List<KeyValue> keyValues = this.kvClient.get(key).get(readTimeoutMs, TimeUnit.MILLISECONDS).getKvs();
                if (!keyValues.isEmpty()) {
                    KeyValue keyValue = keyValues.get(0);
                    String configValue = keyValue.getValue().toString(StandardCharsets.UTF_8);

                    logger.debug("监听平台配置: 查询到平台配置, {}, {}", configKey, configValue);

                    AiriotConfig config = gson.fromJson(configValue, AiriotConfig.class);
                    if (lastConfig == null) {
                        logger.debug("监听平台配置: 第一次查询配置, 不处理, {}", configKey);
                        lastConfig = config;
                    } else if (!lastConfig.equals(config)) {
                        logger.info("监听平台配置: 配置发生变化, {}, 变化前: {}, 变化后: {}", configKey, lastConfig, config);
                        this.applicationEventPublisher.publishEvent(new RefreshEvent("etcd", configKey, configValue));
                        lastConfig = config;
                    } else {
                        logger.debug("监听平台配置: 配置未变化, {}", configKey);
                    }
                } else {
                    logger.debug("监听平台配置: 未查询到配置, {}", configKey);
                }
            } catch (InterruptedException e) {
                logger.info("监听平台配置: 已取消, {}", configKey);
                break;
            } catch (TimeoutException e) {
                logger.warn("监听平台配置: 请求平台配置超时, {}", configKey);
            } catch (Exception e) {
                logger.warn("监听平台配置: 请求平台配置异常, {}", configKey, e);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(intervalMs);
            } catch (InterruptedException e) {
                logger.info("监听平台配置: 已取消, {}", configKey);
                break;
            }
        }
    }

    private void watch() {
        String configKey = this.properties.getConfigKey();
        logger.info("启动 etcd 配置事件监听: {}", configKey);

        this.watcher = this.watchClient.watch(
                ByteSequence.from(configKey, StandardCharsets.UTF_8),
                WatchOption.newBuilder().withNoDelete(true).build(),
                response -> {
                    for (WatchEvent event : response.getEvents()) {
                        if (!WatchEvent.EventType.PUT.equals(event.getEventType())) {
                            return;
                        }

                        KeyValue config = event.getKeyValue();
                        String configValue = config.getValue().toString(StandardCharsets.UTF_8);

                        logger.info("etcd 配置发生变化: {}", configKey);
                        logger.debug("etcd 配置发生变化: {}, {}", configKey, configValue);

                        Thread t = new Thread(() -> {
                            this.applicationEventPublisher.publishEvent(new RefreshEvent("etcd", configKey, configValue));
                        });
                        t.setDaemon(true);
                        t.start();
                    }
                },
                throwable -> {
                    logger.error("etcd 配置事件监听异常", throwable);
                });

        logger.info("启动 etcd 配置事件监听: 已启动");
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(true, false)) {
            return;
        }

        if (this.watchThread != null) {
            this.watchThread.interrupt();
            this.watchThread = null;
        }

        if (this.watcher != null) {
            this.watcher.close();
            this.watcher = null;
        }

        if (this.watchClient != null) {
            this.watchClient.close();
            this.watchClient = null;
        }

        if (this.kvClient != null) {
            this.kvClient.close();
            this.kvClient = null;
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }
}
