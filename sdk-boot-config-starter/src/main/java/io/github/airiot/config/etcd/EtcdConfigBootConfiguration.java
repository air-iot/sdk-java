package io.github.airiot.config.etcd;


import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EtcdConfigProperties.class)
@ConditionalOnProperty(prefix = "airiot.config.etcd", value = "enabled", havingValue = "true", matchIfMissing = true)
public class EtcdConfigBootConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Client etcdClient(EtcdConfigProperties properties) {
        if (CollectionUtils.isEmpty(properties.getEndpoints())) {
            throw new IllegalArgumentException("initialize config from etcd failed, the endpoints is empty");
        }

        String[] endpoints = properties.getEndpoints().toArray(new String[0]);
        return Client.builder()
                .endpoints(endpoints)
                .connectTimeout(properties.getConnectTimeout())
                .retryMaxDuration(properties.getRetryMax())
                .user(ByteSequence.from(properties.getUsername().getBytes(StandardCharsets.UTF_8)))
                .password(ByteSequence.from(properties.getPassword().getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    @Bean
    public EtcdPropertySourceLocator etcdPropertiesSource(Client client, EtcdConfigProperties properties) {
        return new EtcdPropertySourceLocator(client, properties);
    }
}
