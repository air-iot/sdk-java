package io.github.airiot.config.lite;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * etcd 配置管理. 从 etcd 中加载相关配置到上下文中
 */
public class LitePropertySourceLocator implements PropertySourceLocator {

    private final LiteAPIConfig config;

    public LitePropertySourceLocator(LiteAPIConfig config) {
        this.config = config;
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        LitePropertySource propertySource = new LitePropertySource("airiot-lite", this.config);
        propertySource.init();
        return propertySource;
    }
}
