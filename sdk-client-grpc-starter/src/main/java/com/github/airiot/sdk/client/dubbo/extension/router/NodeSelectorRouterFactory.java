package com.github.airiot.sdk.client.dubbo.extension.router;

import com.github.airiot.sdk.client.properties.ClientProperties;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.config.spring.extension.SpringExtensionInjector;
import org.apache.dubbo.rpc.cluster.CacheableRouterFactory;
import org.apache.dubbo.rpc.cluster.Router;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.context.ApplicationContext;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

@Activate(group = CONSUMER)
public class NodeSelectorRouterFactory extends CacheableRouterFactory {

    private final ApplicationContext applicationContext;

    public NodeSelectorRouterFactory(ApplicationModel applicationModel) {
        SpringExtensionInjector springExtensionInjector = SpringExtensionInjector.get(applicationModel);
        this.applicationContext = springExtensionInjector.getContext();
    }

    @Override
    protected Router createRouter(URL url) {
        ClientProperties properties = this.applicationContext.getBean(ClientProperties.class);
        return new NodeSelectorRouter(url, properties);
    }
}
