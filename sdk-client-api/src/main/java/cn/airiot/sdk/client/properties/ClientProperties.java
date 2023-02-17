package cn.airiot.sdk.client.properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;


/**
 * 客户端配置
 */
@ConfigurationProperties(prefix = ClientProperties.PREFIX)
public class ClientProperties implements InitializingBean {

    public static final String PREFIX = "airiot.client";

    /**
     * 默认配置.
     * <br>
     * 取 {@link #services} 中与该名称相同的 key 的配置作为默认服务配置, 当服务未定义配置时使用默认配置
     */
    @NestedConfigurationProperty
    private ServiceConfig defaultConfig = new ServiceConfig(true);

    /**
     * 服务配置列表
     */
    @NestedConfigurationProperty
    private Map<ServiceType, ServiceConfig> services = new HashMap<>();

    public ServiceConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(ServiceConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public Map<ServiceType, ServiceConfig> getServices() {
        return services;
    }

    public void setServices(Map<ServiceType, ServiceConfig> services) {
        this.services = services;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 配置加载完成后, 进行相关处理

        // 如果启用了默认配置
        if (this.defaultConfig.isEnabled()) {
            for (Map.Entry<ServiceType, ServiceConfig> entry : this.services.entrySet()) {
                this.mergeServiceConfig(entry.getValue(), defaultConfig);
            }
        }
    }

    private void mergeServiceConfig(ServiceConfig config, ServiceConfig defaultConfig) {
        // 如果设置为不继承, 或者当前服务已经配置选择器
        if (!config.isInherit() || !config.getSelectors().isEmpty()) {
            return;
        }
        config.setSelectors(defaultConfig.getSelectors());
    }
}
