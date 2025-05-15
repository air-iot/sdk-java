package io.github.airiot.config.etcd;

import io.etcd.jetcd.Client;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * etcd 配置管理. 从 etcd 中加载相关配置到上下文中
 */
public class EtcdPropertySourceLocator implements PropertySourceLocator {

    private final Client client;
    private final EtcdConfigProperties properties;

    public EtcdPropertySourceLocator(Client client, EtcdConfigProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        EtcdPropertySource propertySource = new EtcdPropertySource("airiot-etcd", this.client, this.properties);
        for (int i = 0; i < this.properties.getRetryTimes(); i++) {
            try {
                propertySource.init();
                return propertySource;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.exit(1);
        return null;
    }
}
