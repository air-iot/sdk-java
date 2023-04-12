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

import io.github.airiot.sdk.flow.plugin.FlowPluginManagement;
import io.github.airiot.sdk.flow.plugin.FlowPlugin;
import io.github.airiot.sdk.flow.plugin.PluginServiceGrpc;
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
@EnableConfigurationProperties({FlowPluginProperties.class})
public class FlowPluginAutoConfiguration {

    @Bean
    public Channel channel(FlowPluginProperties properties) {
        return ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                .usePlaintext()
                .build();
    }

    @Bean
    public PluginServiceGrpc.PluginServiceBlockingStub flowPluginService(Channel channel) {
        return PluginServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public FlowPluginManagement flowPluginApp(FlowPluginProperties properties,
                                              PluginServiceGrpc.PluginServiceBlockingStub flowPluginService,
                                              List<FlowPlugin> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            throw new IllegalStateException("未找到任何流程插件实现");
        }

        List<FlowPlugin<Object>> flowPlugins = new ArrayList<>(plugins.size());
        for (FlowPlugin plugin : plugins) {
            flowPlugins.add(plugin);
        }
        return new FlowPluginManagement(properties, flowPluginService, flowPlugins);
    }
}
