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

package io.github.airiot.sdk.flow.configuration;

import cn.airiot.sdk.client.dubbo.grpc.engine.ExtensionServiceGrpc;
import io.github.airiot.sdk.flow.extension.FlowExtension;
import io.github.airiot.sdk.flow.extension.FlowExtensionManagement;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程插件自动装配类
 */
@Configuration
@EnableConfigurationProperties({FlowExtensionProperties.class})
public class FlowExtensionAutoConfiguration {

    @Bean
    public Channel channel(FlowExtensionProperties properties) {
        return ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                .usePlaintext()
                .build();
    }

    @Bean
    public ExtensionServiceGrpc.ExtensionServiceBlockingStub flowPluginService(Channel channel) {
        return ExtensionServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public FlowExtensionManagement flowExtensionManagement(FlowExtensionProperties properties,
                                                           ExtensionServiceGrpc.ExtensionServiceBlockingStub flowService,
                                                           List<FlowExtension> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            throw new IllegalStateException("未找到任何流程插件实现");
        }

        List<FlowExtension<Object>> flowExtensions = new ArrayList<>(extensions.size());
        for (FlowExtension extension : extensions) {
            flowExtensions.add(extension);
        }
        return new FlowExtensionManagement(properties, flowService, flowExtensions);
    }
}
