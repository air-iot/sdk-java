package io.github.airiot.sdk.datarelay.configuration;

import io.github.airiot.sdk.client.service.core.TimingDataClient;
import io.github.airiot.sdk.datarelay.application.DataRelayApp;
import io.github.airiot.sdk.datarelay.application.DataRelayAppListener;
import io.github.airiot.sdk.datarelay.configuration.properties.DataRelayAppProperties;
import io.github.airiot.sdk.datarelay.configuration.properties.DataRelayGrpcProperties;
import io.github.airiot.sdk.datarelay.configuration.properties.DataRelayMQProperties;
import io.github.airiot.sdk.datarelay.grpc.DataRelayInstanceServiceGrpc;
import io.github.airiot.sdk.datarelay.subscriber.DataSubscriber;
import io.github.airiot.sdk.datarelay.subscriber.impl.KafkaDataSubscriber;
import io.github.airiot.sdk.datarelay.subscriber.impl.MQTTDataSubscriber;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties({DataRelayAppProperties.class, DataRelayGrpcProperties.class, DataRelayMQProperties.class})
public class DataRelayAutoConfiguration {

    @Bean
    public Channel channel(DataRelayGrpcProperties properties) {
        return ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                .usePlaintext()
                .maxInboundMessageSize(properties.getMaxInboundMessageSize())
                .build();
    }

    @Bean
    public DataRelayInstanceServiceGrpc.DataRelayInstanceServiceBlockingStub dataRelayGrpcClient(Channel channel) {
        return DataRelayInstanceServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    @Order
    public DataRelayAppListener grpcDriverEventListener(DataRelayGrpcProperties grpcProperties,
                                                        DataRelayAppProperties appProperties,
                                                        ObjectProvider<DataRelayApp> driverApp,
                                                        DataRelayInstanceServiceGrpc.DataRelayInstanceServiceBlockingStub dataRelayGrpcClient) {
        DataRelayApp<Object> app = driverApp.getIfUnique();
        if (app == null) {
            throw new BeanCreationException("未找到或找到多个 DriverApp 实例");
        }
        return new DataRelayAppListener(
                appProperties, grpcProperties,
                app, dataRelayGrpcClient
        );
    }

    @Configuration
    static class DriverDataSenderConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "mqtt", matchIfMissing = true)
        @ConditionalOnMissingBean(DataSubscriber.class)
        public DataSubscriber mqttDataSubscriber(TimingDataClient timingDataClient,
                                                 DataRelayAppProperties appProperties,
                                                 DataRelayMQProperties mqProperties) {
            return new MQTTDataSubscriber(timingDataClient, appProperties, mqProperties.getMqtt());
        }

        @Bean
        @ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "kafka")
        @ConditionalOnMissingBean(DataSubscriber.class)
        public DataSubscriber kafkaDataSubscriber(TimingDataClient timingDataClient,
                                                  DataRelayAppProperties appProperties,
                                                  DataRelayMQProperties mqProperties) {
            return new KafkaDataSubscriber(timingDataClient, appProperties, mqProperties.getKafka());
        }
    }

    @Configuration
    @ConditionalOnClass(LoggerFactory.class)
    static class LoggerConfiguration {

        private final DataRelayAppProperties properties;

        public LoggerConfiguration(DataRelayAppProperties properties) {
            this.properties = properties;
        }

        @PostConstruct
        public void init() {
            LoggerContexts.setDefaultProjectId(properties.getProjectId());
            LoggerContexts.setDefaultService(properties.getProjectId() + "-" + properties.getInstanceId() + "-" + properties.getId());
        }
    }
}
