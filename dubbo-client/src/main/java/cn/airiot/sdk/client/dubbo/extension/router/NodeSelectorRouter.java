package cn.airiot.sdk.client.dubbo.extension.router;

import cn.airiot.sdk.client.dubbo.configuration.properties.DubboClientProperties;
import cn.airiot.sdk.client.dubbo.configuration.properties.ServiceConfig;
import cn.airiot.sdk.client.dubbo.extension.registry.ServiceConstants;
import cn.airiot.sdk.client.dubbo.extension.utils.DubboUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Router;
import org.apache.dubbo.rpc.cluster.router.RouterResult;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


/**
 * 服务实例选择器.
 * <br>
 * 在服务调用时, 根据服务配置的 {@link ServiceConfig#getSelectors()} 信息在服务实例列表中筛选出符合条件服务实例.
 */
public class NodeSelectorRouter implements Router {

    private final URL url;
    private final DubboClientProperties properties;

    public NodeSelectorRouter(URL url, DubboClientProperties properties) {
        this.url = url;
        this.properties = properties;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isRuntime() {
        return true;
    }

    @Override
    public boolean isForce() {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public <T> RouterResult<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation, boolean needToPrintMessage) throws RpcException {
        List<Invoker<T>> filteredInvokers = this.route(invokers, url);
        return new RouterResult<>(filteredInvokers);
    }

    private <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url) {
        String serviceName = DubboUtils.getServiceName(url);
        ServiceConfig service = this.getServiceConfig(serviceName);
        if (service == null) {
            return invokers;
        }
        
        if (CollectionUtils.isEmpty(service.getSelectors())) {
            return invokers;
        }

        List<Invoker<T>> filteredInvokers = new ArrayList<>(1);

        List<ServiceConfig.Selector> selectors = service.getSelectors();
        for (ServiceConfig.Selector selector : selectors) {
            for (Invoker<T> invoker : invokers) {
                URL inst = invoker.getUrl();
                Map<String, String> metadata = (Map<String, String>) inst.getAttribute(ServiceConstants.METADATA_KEY);
                if (metadata == null || metadata.isEmpty()) {
                    continue;
                }

                if (this.containsAllMetadata(metadata, selector)) {
                    filteredInvokers.add(invoker);
                }
            }

            if (!filteredInvokers.isEmpty()) {
                break;
            }
        }

        if (!filteredInvokers.isEmpty()) {
            return filteredInvokers;
        }

        switch (service.getPolicy()) {
            case ALL:
                return invokers;
            case FIRST:
                return Collections.singletonList(invokers.get(0));
            case RANDOM:
                return Collections.singletonList(invokers.get(ThreadLocalRandom.current().nextInt() % invokers.size()));
            case REJECT:
                return Collections.emptyList();
            default:
                throw new IllegalStateException("节点选择器: 选择器执行策略, policy = " + service.getPolicy().name());
        }
    }

    private boolean containsAllMetadata(Map<String, String> metadata, ServiceConfig.Selector selector) {
        Map<String, String> selectorMetadata = selector.getMetadata();
        if (CollectionUtils.isEmpty(selectorMetadata) || metadata.size() < selectorMetadata.size()) {
            return false;
        }

        for (Map.Entry<String, String> entry : selectorMetadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!StringUtils.hasText(value)) {
                continue;
            }

            if (!metadata.containsKey(key)) {
                return false;
            }

            if (!value.equals(metadata.get(key))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取指定服务的配置
     *
     * @param serviceName 服务名称
     * @return 服务配置
     */
    private ServiceConfig getServiceConfig(String serviceName) {
        Map<String, ServiceConfig> services = properties.getServices();
        if (services.containsKey(serviceName)) {
            return services.get(serviceName);
        }
        return services.get(properties.getDefaultConfigName());
    }
}
