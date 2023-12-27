/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.airiot.sdk.driver.configuration;


import io.github.airiot.sdk.driver.DriverApp;
import io.github.airiot.sdk.driver.GlobalContext;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverListenerProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverMQProperties;
import io.github.airiot.sdk.driver.data.DataHandler;
import io.github.airiot.sdk.driver.data.DataHandlerChain;
import io.github.airiot.sdk.driver.data.DataSender;
import io.github.airiot.sdk.driver.data.DefaultDataHandlerChain;
import io.github.airiot.sdk.driver.data.handlers.TagValueCache;
import io.github.airiot.sdk.driver.data.impl.AmqpDataSender;
import io.github.airiot.sdk.driver.data.impl.KafkaDataSender;
import io.github.airiot.sdk.driver.data.impl.MQTTDataSender;
import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import io.github.airiot.sdk.driver.listener.DriverEventListener;
import io.github.airiot.sdk.driver.listener.GrpcDriverEventListener;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@EnableConfigurationProperties({DriverAppProperties.class, DriverDataProperties.class, DriverMQProperties.class, DriverListenerProperties.class,})
public class DriverAutoConfiguration {

    @Bean
    public TagValueCache tagValueCache() {
        return new TagValueCache();
    }

    @Bean
    @ConditionalOnMissingBean(DataHandlerChain.class)
    public DataHandlerChain dataHandlerChain(TagValueCache tagValueCache, ObjectProvider<DataHandler> handlers) {
        List<DataHandler> dataHandlers = handlers.stream().collect(Collectors.toList());
        return new DefaultDataHandlerChain(tagValueCache, dataHandlers);
    }

    @Bean
    public GlobalContext globalContext() {
        return new GlobalContext();
    }

    @Configuration
    @ConditionalOnProperty(value = DriverAppProperties.DRIVER_ENABLE_PROPERTY, havingValue = "true", matchIfMissing = true)
    static class DriverEventListenerConfiguration {

        @Bean
        public Channel channel(DriverListenerProperties properties) {
            return ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                    .usePlaintext()
                    .maxInboundMessageSize(properties.getMaxInboundMessageSize())
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
                    driverProperties, driverListenerProperties,
                    globalContext, app, driverGrpcClient
            );
        }
    }

    @Configuration
    @ConditionalOnProperty(value = DriverAppProperties.DRIVER_ENABLE_PROPERTY, havingValue = "true", matchIfMissing = true)
    static class DriverDataSenderConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "mqtt", matchIfMissing = true)
        @ConditionalOnMissingBean(DataSender.class)
        public DataSender mqttDataSender(DriverDataProperties driverDataProperties,
                                         DriverAppProperties driverAppProperties,
                                         DriverMQProperties properties,
                                         DataHandlerChain dataHandlerChain, GlobalContext globalContext,
                                         DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
            return new MQTTDataSender(dataHandlerChain, driverDataProperties, driverAppProperties,
                    properties.getMqtt(), globalContext, driverGrpcClient);
        }

        @Bean
        @ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "amqp")
        @ConditionalOnMissingBean(DataSender.class)
        public DataSender amqpDataSender(DriverDataProperties driverDataProperties,
                                         DriverAppProperties driverAppProperties,
                                         DriverMQProperties mqProperties,
                                         DataHandlerChain dataHandlerChain, GlobalContext globalContext,
                                         DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
            return new AmqpDataSender(driverDataProperties, driverAppProperties,
                    dataHandlerChain, mqProperties.getAmqp(), globalContext, driverGrpcClient);
        }

        @Bean
        @ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "kafka")
        @ConditionalOnMissingBean(DataSender.class)
        public DataSender kafkaDataSender(DriverDataProperties driverDataProperties,
                                          DriverAppProperties driverAppProperties,
                                          DriverMQProperties mqProperties,
                                          DataHandlerChain dataHandlerChain, GlobalContext globalContext,
                                          DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
            return new KafkaDataSender(driverDataProperties, driverAppProperties,
                    mqProperties.getKafka(), globalContext, dataHandlerChain, driverGrpcClient);
        }
    }

    @Configuration
    @ConditionalOnClass(LoggerFactory.class)
    static class LoggerConfiguration {

        private final DriverAppProperties properties;

        public LoggerConfiguration(DriverAppProperties properties) {
            this.properties = properties;
        }

        @PostConstruct
        public void init() {
            LoggerContexts.setDefaultProjectId(properties.getProjectId());
            LoggerContexts.setDefaultService(properties.getProjectId() + "-" + properties.getInstanceId() + "-" + properties.getId());
        }
    }
}

