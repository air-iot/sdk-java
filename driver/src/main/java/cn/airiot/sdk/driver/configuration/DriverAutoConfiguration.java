package cn.airiot.sdk.driver.configuration;


import cn.airiot.sdk.driver.DriverApp;
import cn.airiot.sdk.driver.GlobalContext;
import cn.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import cn.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import cn.airiot.sdk.driver.configuration.properties.DriverListenerProperties;
import cn.airiot.sdk.driver.data.DataHandler;
import cn.airiot.sdk.driver.data.DataHandlerChain;
import cn.airiot.sdk.driver.data.DataSender;
import cn.airiot.sdk.driver.data.DefaultDataHandlerChain;
import cn.airiot.sdk.driver.data.impl.AmqpDataSender;
import cn.airiot.sdk.driver.data.impl.MQTTDataSender;
import cn.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import cn.airiot.sdk.driver.listener.DriverEventListener;
import cn.airiot.sdk.driver.listener.impl.GrpcDriverEventListener;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 驱动配置类
 */
@Configuration
@ConditionalOnProperty(prefix = "airiot.driver", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({DriverAppProperties.class, DriverDataProperties.class, DriverListenerProperties.class,})
public class DriverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataHandlerChain.class)
    public DataHandlerChain dataHandlerChain(ObjectProvider<DataHandler> handlers) {
        List<DataHandler> dataHandlers = handlers.stream().collect(Collectors.toList());
        return new DefaultDataHandlerChain(dataHandlers);
    }

    @Bean
    public GlobalContext globalContext() {
        return new GlobalContext();
    }

    @Configuration
    @ConditionalOnProperty(prefix = "airiot.driver", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class DriverEventListenerConfiguration {

        @Bean
        public Channel channel(DriverListenerProperties properties) {
            DriverListenerProperties.Grpc grpcProperties = properties.getGrpc();
            return ManagedChannelBuilder.forAddress(grpcProperties.getHost(), grpcProperties.getPort())
                    .usePlaintext()
                    .build();
        }

        @Bean
        public DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient(Channel channel) {
            return DriverServiceGrpc.newBlockingStub(channel);
        }
        
        @Bean
        @Order
        @ConditionalOnProperty(prefix = "airiot.driver.listener", name = "type", havingValue = "grpc", matchIfMissing = true)
        public DriverEventListener grpcDriverEventListener(DriverListenerProperties driverListenerProperties,
                                                           DriverAppProperties driverProperties,
                                                           ObjectProvider<DriverApp> driverApp, GlobalContext globalContext,
                                                           DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
            DriverApp<Object, Object, Object> app = driverApp.getIfUnique();
            if (app == null) {
                throw new BeanCreationException("未找到或找到多个 DriverApp 实例");
            }
            return new GrpcDriverEventListener(
                    driverProperties, driverListenerProperties.getGrpc(),
                    globalContext, app, driverGrpcClient);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "airiot.driver", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class DriverDataSenderConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "airiot.driver.data", name = "type", havingValue = "mqtt", matchIfMissing = true)
        @ConditionalOnMissingBean(DataSender.class)
        public DataSender mqttDataSender(DriverAppProperties driverAppProperties,
                                         DriverDataProperties properties,
                                         DataHandlerChain dataHandlerChain, GlobalContext globalContext,
                                         DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
            return new MQTTDataSender(dataHandlerChain, driverAppProperties,
                    properties.getMqtt(), globalContext, driverGrpcClient);
        }

        @Bean
        @ConditionalOnProperty(prefix = "airiot.driver.data", name = "type", havingValue = "amqp")
        @ConditionalOnMissingBean(DataSender.class)
        public DataSender amqpDataSender(DriverAppProperties appProperties, DriverDataProperties dataProperties,
                                         DataHandlerChain dataHandlerChain, GlobalContext globalContext,
                                         DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
            return new AmqpDataSender(appProperties.getProjectId(),
                    dataHandlerChain, dataProperties.getAmqp(), globalContext, driverGrpcClient);
        }
    }

}

