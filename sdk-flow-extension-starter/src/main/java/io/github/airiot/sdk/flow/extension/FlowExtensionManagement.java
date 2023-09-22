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

package io.github.airiot.sdk.flow.extension;

import cn.airiot.sdk.client.dubbo.grpc.engine.*;
import io.github.airiot.sdk.flow.configuration.FlowExtensionProperties;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流程插件管理器
 */
public class FlowExtensionManagement implements SmartLifecycle {

    private final Logger logger = LoggerFactory.getLogger(FlowExtensionManagement.class);
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final FlowExtensionProperties properties;
    private final ExtensionServiceGrpc.ExtensionServiceBlockingStub flowExtensionService;
    private final List<FlowExtensionDelegate> extensions;
    private final List<FlowExtensionHandler> handlers;

    private final Channel channel;

    private final ThreadPoolExecutor executor;

    /**
     * 连接线程
     */
    private Thread connectThread;
    /**
     * 心跳线程
     */
    private Thread heartbeatThread;

    public FlowExtensionManagement(FlowExtensionProperties properties,
                                   ExtensionServiceGrpc.ExtensionServiceBlockingStub flowExtensionService,
                                   List<FlowExtension<Object>> extensions) {
        this.properties = properties;
        this.flowExtensionService = flowExtensionService;
        this.extensions = new ArrayList<>(extensions.size());

        int maxThreads = properties.getMaxThreads() <= 0 ? Runtime.getRuntime().availableProcessors() : properties.getMaxThreads();
        int coreThreads = maxThreads / 2 + 1;
        this.executor = new ThreadPoolExecutor(coreThreads, maxThreads, 15, TimeUnit.MINUTES, new ArrayBlockingQueue<>(coreThreads));

        Map<String, Class<?>> extensionIds = new HashMap<>();
        for (FlowExtension<Object> extension : extensions) {
            String id = extension.getId();
            if (!StringUtils.hasText(id)) {
                throw new IllegalArgumentException("the 'id' of extension " + extension.getClass().getName() + " cannot be empty");
            }
            if (extensionIds.containsKey(id)) {
                throw new IllegalArgumentException("the extension '" + id + "' has bean used by " + extensionIds.get(id).getName());
            }

            extensionIds.put(id, extension.getClass());
            this.extensions.add(new FlowExtensionDelegate(extension));
        }

        this.handlers = new ArrayList<>(extensions.size());

        this.channel = ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                .usePlaintext()
                .build();
    }

    @Override
    public void start() {
        this.running.set(true);

        // 执行各个插件的 onStart 方法
        for (FlowExtensionDelegate extension : this.extensions) {
            extension.onStart();
        }

        this.startConnect();
    }

    @Override
    public void stop() {
        this.running.set(false);

        for (FlowExtensionHandler handler : this.handlers) {
            handler.close();
        }
        this.handlers.clear();

        // 执行各个插件的 onStop 方法
        for (FlowExtensionDelegate extension : this.extensions) {
            extension.onStop();
        }

        this.executor.shutdown();

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
        if (!this.running.get()) {
            logger.warn("服务已停止, 无法重连");
            return;
        }

        for (FlowExtensionHandler handler : this.handlers) {
            handler.close();
        }
        this.handlers.clear();

        // 执行状态变更回调函数
        for (FlowExtensionDelegate value : this.extensions) {
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

    private Metadata createMetadata(FlowExtension<?> extension) {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("id", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(extension.getId().getBytes(StandardCharsets.UTF_8))
        );
        metadata.put(
                Metadata.Key.of("name", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(extension.getName().getBytes(StandardCharsets.UTF_8))
        );
        return metadata;
    }

    private void connect() {
        long retryInterval = properties.getRetryInterval().toMillis();

        logger.info("开始连接流程引擎服务, 重试间隔: {}ms", retryInterval);

        int retryTimes = 0;
        while (this.running.get()) {
            retryTimes++;

            logger.info("第 {} 次连接流程引擎服务", retryTimes);

            try {
                for (FlowExtensionDelegate extension : this.extensions) {
                    logger.info("注册流程扩展节点: id={}, name={}", extension.getId(), extension.getName());

                    ClientCall<ExtensionResult, ExtensionSchemaRequest> schemaCall = channel.newCall(
                            ExtensionServiceGrpc.getSchemaStreamMethod(),
                            CallOptions.DEFAULT.withWaitForReady()
                    );

                    ClientCall<ExtensionResult, ExtensionRunRequest> runCall = channel.newCall(
                            ExtensionServiceGrpc.getRunStreamMethod(),
                            CallOptions.DEFAULT.withWaitForReady()
                    );

                    FlowExtensionHandler handler = new FlowExtensionHandler(extension, this.executor, schemaCall, runCall);
                    schemaCall.start(handler.getSchemaHandler(), this.createMetadata(extension));
                    schemaCall.request(Integer.MAX_VALUE);

                    runCall.start(handler.getRunHandler(), this.createMetadata(extension));
                    runCall.request(Integer.MAX_VALUE);

                    this.handlers.add(handler);

                    logger.info("注册流程扩展节点: 成功, id={}, name={}", extension.getId(), extension.getName());
                }

                // 所有插件都注册成功后, 执行回调函数
                for (FlowExtensionDelegate extension : this.extensions) {
                    extension.onConnectionStateChange(true);
                }

                // 开启心跳
                this.startHeartbeat();

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

        logger.info("心跳任务已启动, 心跳间隔 {}ms", heartbeatInterval);

        int failureTimes = 0;
        while (this.running.get()) {
            try {
                TimeUnit.MILLISECONDS.sleep(heartbeatInterval);
            } catch (InterruptedException e) {
                logger.info("发送心跳任务被中断");
                return;
            }

            try {
                for (FlowExtensionDelegate extension : this.extensions) {
                    String extensionId = extension.getId();
                    logger.info("发送心跳: id={}", extensionId);

                    ExtensionHealthCheckResponse response = this.flowExtensionService.healthCheck(ExtensionHealthCheckRequest.newBuilder()
                            .setId(extension.getId())
                            .build());
                    logger.info("接收到心跳响应, id={}, status={}", extensionId, response.getStatus());

                    // 如果心跳响应不是 SERVING, 则表明服务出现问题, 重新连接
                    if (!ExtensionHealthCheckResponse.ServingStatus.SERVING.equals(response.getStatus())) {
                        logger.error("心跳检测到服务状态异常, 重新连接. status={}", response.getStatus());
                        this.reconnect();
                        return;
                    }

                    failureTimes = 0;
                }
            } catch (Exception e) {
                logger.error("发送心跳异常:", e);
                failureTimes++;
                if (failureTimes >= 3) {
                    logger.error("连续 3 次心跳发送异常, 中断心跳并重新连接");
                    this.reconnect();
                    return;
                }
            }
        }
    }
}
