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

package io.github.airiot.sdk.flow.plugin;

import io.github.airiot.sdk.flow.configuration.FlowPluginProperties;
import io.grpc.*;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流程插件管理器
 */
public class FlowPluginManagement implements SmartLifecycle {

    private final Logger logger = LoggerFactory.getLogger(FlowPluginManagement.class);
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final FlowPluginProperties properties;
    private final PluginServiceGrpc.PluginServiceBlockingStub flowPluginService;
    private final List<FlowPluginDelegate> plugins;

    private final Channel channel;

    /**
     * 连接线程
     */
    private Thread connectThread;
    /**
     * 心跳线程
     */
    private Thread heartbeatThread;

    public FlowPluginManagement(FlowPluginProperties properties,
                                PluginServiceGrpc.PluginServiceBlockingStub flowPluginService,
                                List<FlowPlugin<Object>> plugins) {
        this.properties = properties;
        this.flowPluginService = flowPluginService;
        this.plugins = new ArrayList<>(plugins.size());

        Map<String, Class<?>> pluginNames = new HashMap<>();
        for (FlowPlugin<Object> plugin : plugins) {
            String name = plugin.getName();
            if (!StringUtils.hasText(name)) {
                throw new IllegalArgumentException("the 'name' of plugin " + plugin.getClass().getName() + " cannot be empty");
            }
            if (pluginNames.containsKey(name)) {
                throw new IllegalArgumentException("the plugin '" + name + "' has bean used by " + pluginNames.get(name).getName());
            }

            pluginNames.put(name, plugin.getClass());
            this.plugins.add(new FlowPluginDelegate(plugin));
        }

        this.channel = ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                .usePlaintext()
                .build();
    }

    @Override
    public void start() {
        this.running.set(true);

        // 执行各个插件的 onStart 方法
        for (FlowPluginDelegate plugin : this.plugins) {
            plugin.onStart();
        }

        this.startConnect();
    }

    @Override
    public void stop() {
        this.running.set(false);

        // 执行各个插件的 onStop 方法
        for (FlowPluginDelegate plugin : this.plugins) {
            plugin.onStop();
        }

        if (this.connectThread != null) {
            this.connectThread.interrupt();
            this.connectThread = null;
        }

        if (this.heartbeatThread != null) {
            this.heartbeatThread.interrupt();
            this.heartbeatThread = null;
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 重新连接流程引擎服务
     */
    private void reconnect() {
        // 执行状态变更回调函数
        for (FlowPluginDelegate value : this.plugins) {
            value.onConnectionStateChange(false);
        }

        this.startConnect();
    }

    private void startConnect() {
        if (this.connectThread != null) {
            this.connectThread.interrupt();
        }

        this.connectThread = new Thread(this::connect, "Flow-Connector");
        this.connectThread.setDaemon(false);
        this.connectThread.start();
    }

    private void connect() {
        long retryInterval = properties.getRetryInterval().toMillis();

        logger.info("开始连接流程引擎服务, 重试间隔: {}ms", retryInterval);

        int retryTimes = 0;
        while (this.running.get()) {
            retryTimes++;

            logger.info("第 {} 次连接流程引擎服务", retryTimes);

            try {
                for (FlowPluginDelegate plugin : this.plugins) {
                    logger.info("注册插件: name={}, mode={}", plugin.getName(), plugin.getPluginType().getType());
                    Metadata metadata = new Metadata();
                    metadata.put(
                            Metadata.Key.of("name", Metadata.ASCII_STRING_MARSHALLER),
                            Hex.encodeHexString(plugin.getName().getBytes(StandardCharsets.UTF_8))
                    );
                    metadata.put(
                            Metadata.Key.of("mode", Metadata.ASCII_STRING_MARSHALLER),
                            Hex.encodeHexString(plugin.getPluginType().getType().getBytes(StandardCharsets.UTF_8))
                    );

                    ClientCall<FlowResponse, FlowRequest> call = channel.newCall(
                            PluginServiceGrpc.getRegisterMethod(),
                            CallOptions.DEFAULT.withWaitForReady()
                    );
                    FlowPluginHandler startHandler = new FlowPluginHandler(call, plugin);
                    call.start(startHandler, metadata);
                    call.request(Integer.MAX_VALUE);

                    logger.info("注册插件: 成功, name={}, mode={}", plugin.getName(), plugin.getPluginType());

                    this.startHeartbeat();
                }

                // 所有插件都注册成功后, 执行回调函数
                for (FlowPluginDelegate plugin : this.plugins) {
                    plugin.onConnectionStateChange(true);
                }

                break;
            } catch (Exception e) {
                logger.error("第 {} 次连接流程引擎服务失败", retryTimes, e);
            }

            try {
                TimeUnit.MICROSECONDS.sleep(retryInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * 启动心跳任务
     */
    private void startHeartbeat() {
        if (this.heartbeatThread != null) {
            this.heartbeatThread.interrupt();
        }

        logger.info("启动心跳任务");

        this.heartbeatThread = new Thread(this::heartbeat, "Flow-Heartbeat");
        this.heartbeatThread.start();
    }

    /**
     * 定时发送心跳任务
     */
    private void heartbeat() {
        long heartbeatInterval = this.properties.getHeartbeatInterval().toMillis();
        String pluginName = this.plugins.get(0).getName();

        logger.info("心跳任务已启动, 心跳间隔 {}ms", heartbeatInterval);

        int failureTimes = 0;
        while (this.running.get()) {
            logger.info("发送心跳: name={}", pluginName);
            try {
                HealthCheckResponse response = this.flowPluginService.healthCheck(HealthCheckRequest.newBuilder()
                        .setName(pluginName)
                        .build());
                logger.info("接收到心跳响应, name={}, status={}", pluginName, response.getStatus());

                if (!response.getErrorsList().isEmpty()) {
                    for (Error error : response.getErrorsList()) {
                        logger.error("心跳响应错误, code={}, message={}", error.getCode(), error.getMessage());
                    }
                }

                // 如果心跳响应不是 SERVING, 则表明服务出现问题, 重新连接
                if (!HealthCheckResponse.ServingStatus.SERVING.equals(response.getStatus())) {
                    logger.error("心跳检测到服务状态异常, 重新连接. status={}", response.getStatus());
                    this.reconnect();
                    return;
                }

                failureTimes = 0;
            } catch (Exception e) {
                logger.error("发送心跳异常:", e);
                failureTimes++;
                if (failureTimes >= 3) {
                    logger.error("连续 3 次心跳发送异常, 中断心跳并重新连接");
                    this.reconnect();
                    return;
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(heartbeatInterval);
            } catch (InterruptedException e) {
                logger.info("发送心跳任务被中断");
                break;
            }
        }
    }
}
