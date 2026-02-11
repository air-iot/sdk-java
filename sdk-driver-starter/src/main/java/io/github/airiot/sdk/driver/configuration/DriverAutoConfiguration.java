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
import io.github.airiot.sdk.driver.ai.AIServer;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverListenerProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverMQProperties;
import io.github.airiot.sdk.driver.data.*;
import io.github.airiot.sdk.driver.data.handlers.TagValueCache;
import io.github.airiot.sdk.driver.data.impl.AmqpDataSender;
import io.github.airiot.sdk.driver.data.impl.KafkaDataSender;
import io.github.airiot.sdk.driver.data.impl.LocalDataSender;
import io.github.airiot.sdk.driver.data.impl.MQTTDataSender;
import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import io.github.airiot.sdk.driver.listener.DriverEventListener;
import io.github.airiot.sdk.driver.listener.GrpcDriverEventListener;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 驱动配置类
 */
@Configuration
@ImportRuntimeHints(GraalvmRuntimeHits.class)
@EnableConfigurationProperties({DriverAppProperties.class, DriverDataProperties.class, DriverMQProperties.class, DriverListenerProperties.class})
public class DriverAutoConfiguration {

    public DriverAutoConfiguration(DriverAppProperties properties) {
        LoggerContexts.setDefaultProjectId(properties.getProjectId());
        LoggerContexts.setDefaultService(properties.getProjectId() + "-" + properties.getInstanceId() + "-" + properties.getId());
    }

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

    @Bean
    public AIServer aiServer(DriverAppProperties properties,
                             GlobalContext globalContext,
                             ObjectProvider<DriverApp> driverApp,
                             DataDispatchers dispatchers) {
        DriverApp<Object, Object, Object> app = driverApp.getIfUnique();
        if (app == null) {
            throw new BeanCreationException("未找到或找到多个 DriverApp 实例");
        }
        return new AIServer(properties, globalContext, app, dispatchers);
    }

    @Bean
    public DriverEventListener driverEventListener(DriverListenerProperties properties,
                                                   DriverAppProperties driverProperties,
                                                   ObjectProvider<DriverApp> driverApp,
                                                   GlobalContext globalContext) {
        DriverApp<Object, Object, Object> app = driverApp.getIfUnique();
        if (app == null) {
            throw new BeanCreationException("未找到或找到多个 DriverApp 实例");
        }

        Channel channel = ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                .usePlaintext()
                .maxInboundMessageSize(properties.getMaxInboundMessageSize())
                .disableRetry()
                .build();
        DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient = DriverServiceGrpc.newBlockingStub(channel);

        return new GrpcDriverEventListener(
                driverProperties, properties,
                globalContext, app, driverGrpcClient
        );
    }

    @Bean
    public DataSender dataSender(DriverAppProperties driverAppProperties,
                                 DataHandlerChain dataHandlerChain,
                                 GlobalContext globalContext,
                                 ObjectProvider<DriverServiceGrpc.DriverServiceBlockingStub> driverGrpcClient,
                                 DataWriter dataWriter,
                                 DataDispatchers dispatchers) {
        return new DefaultDataSender(driverAppProperties, globalContext, dataHandlerChain, driverGrpcClient.getIfAvailable(), dataWriter, dispatchers);
    }

    @Bean
    public DataWriter dataWriter(DriverDataProperties driverDataProperties,
                                 DriverAppProperties driverAppProperties,
                                 DriverMQProperties properties) {
        String projectId = driverAppProperties.getProjectId();
        return switch (properties.getType()) {
            case MQTT -> new MQTTDataSender(projectId, driverDataProperties, driverAppProperties, properties.getMqtt());
            case RABBIT -> new AmqpDataSender(projectId, driverDataProperties, properties.getAmqp());
            case KAFKA ->
                    new KafkaDataSender(projectId, driverDataProperties, driverAppProperties, properties.getKafka());
            case LOCAL -> new LocalDataSender(projectId, driverDataProperties);
        };
    }

    @Bean
    public DataDispatchers dispatchers() {
        return new DataDispatchers();
    }
}

