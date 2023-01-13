package cn.airiot.sdk.client.dubbo.configuration.properties;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "dubbo.client")
public class DubboClientProperties implements InitializingBean {

    /**
     * 默认配置的名称.
     * <br>
     * 取 {@link #services} 中与该名称相同的 key 的配置作为默认服务配置, 当服务未定义配置时使用默认配置
     */
    private String defaultConfigName = "default";

    /**
     * 服务配置列表
     */
    private Map<String, ServiceConfig> services = new HashMap<>();

    public String getDefaultConfigName() {
        return defaultConfigName;
    }

    public void setDefaultConfigName(String defaultConfigName) {
        this.defaultConfigName = defaultConfigName;
    }

    public Map<String, ServiceConfig> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceConfig> services) {
        this.services = services;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 配置加载完成后, 进行相关处理

        if (this.services.containsKey(this.defaultConfigName)) {
            ServiceConfig defaultConfig = this.services.get(this.defaultConfigName);
            for (Map.Entry<String, ServiceConfig> entry : this.services.entrySet()) {
                if (this.defaultConfigName.equals(entry.getKey())) {
                    continue;
                }
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
