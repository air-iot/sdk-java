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

package io.github.airiot.sdk.algorithm.configuration;


import io.github.airiot.sdk.algorithm.AlgorithmApp;
import io.github.airiot.sdk.algorithm.AlgorithmManagement;
import io.github.airiot.sdk.algorithm.grpc.algorithm.AlgorithmServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AlgorithmProperties.class, AlgorithmGrpcProperties.class})
public class AlgorithmAutoConfiguration {


    @Bean
    public Channel channel(AlgorithmGrpcProperties properties) {
        return ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                .usePlaintext()
                .build();
    }

    @Bean
    public AlgorithmServiceGrpc.AlgorithmServiceBlockingStub flowPluginService(Channel channel) {
        return AlgorithmServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public AlgorithmManagement algorithmManagement(AlgorithmProperties properties,
                                                   Channel channel,
                                                   AlgorithmServiceGrpc.AlgorithmServiceBlockingStub algorithmService,
                                                   ObjectProvider<AlgorithmApp> app) {
        
        AlgorithmApp algorithmApp = app.getIfUnique();
        if (algorithmApp == null) {
            throw new IllegalArgumentException("未找到 AlgorithmApp 实现类, 请检查是否创建了该类的实现并且注入到 Spring 容器中");
        }
        return new AlgorithmManagement(properties, channel, algorithmService, algorithmApp);
    }
}
