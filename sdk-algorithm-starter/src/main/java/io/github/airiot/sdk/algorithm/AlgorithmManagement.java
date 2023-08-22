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

package io.github.airiot.sdk.algorithm;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.algorithm.annotation.AnnotationUtils;
import io.github.airiot.sdk.algorithm.configuration.AlgorithmProperties;
import io.github.airiot.sdk.algorithm.grpc.algorithm.Error;
import io.github.airiot.sdk.algorithm.grpc.algorithm.*;
import io.grpc.*;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流程插件管理器
 */
public class AlgorithmManagement implements SmartLifecycle {

    private final Logger logger = LoggerFactory.getLogger(AlgorithmManagement.class);
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final AlgorithmProperties properties;
    private final AlgorithmServiceGrpc.AlgorithmServiceBlockingStub algorithmService;
    private final AlgorithmApp app;

    private final Map<String, AlgorithmFunctionDefinition> functions;

    private final Channel channel;

    private final ThreadPoolExecutor executor;

    private AlgorithmHandler handler;
    private SchemaHandler schemaHandler;
    /**
     * 连接线程
     */
    private Thread connectThread;
    /**
     * 心跳线程
     */
    private Thread heartbeatThread;

    public AlgorithmManagement(AlgorithmProperties properties,
                               AlgorithmServiceGrpc.AlgorithmServiceBlockingStub algorithmService,
                               AlgorithmApp algorithmApp) {
        this.properties = properties;
        this.algorithmService = algorithmService;
        this.app = algorithmApp;

        int maxThreads = properties.getMaxThreads() <= 0 ? Runtime.getRuntime().availableProcessors() : properties.getMaxThreads();
        int coreThreads = maxThreads / 2 + 1;
        this.executor = new ThreadPoolExecutor(coreThreads, maxThreads, 15, TimeUnit.MINUTES, new ArrayBlockingQueue<>(coreThreads));

        AlgorithmProperties.AlgorithmGrpc grpc = properties.getAlgorithmGrpc();
        this.channel = ManagedChannelBuilder.forAddress(grpc.getHost(), grpc.getPort())
                .usePlaintext()
                .build();

        this.functions = AnnotationUtils.scanFunctions(algorithmApp);
    }

    @Override
    public void start() {
        if (!this.running.compareAndSet(false, true)) {
            logger.warn("服务已启动");
            return;
        }

        logger.info("启动算法服务");

        this.startConnect();
    }

    @Override
    public void stop() {
        this.running.set(false);

        this.schemaHandler.close();
        this.handler.close();
        this.app.stop();
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
     * 重新连接算法服务
     */
    private void reconnect() {
        if (!this.running.get()) {
            logger.warn("服务已停止, 无法重连");
            return;
        }

        this.startConnect();
    }

    private void startConnect() {
        if (this.connectThread != null) {
            this.connectThread.interrupt();
        }

        this.connectThread = new Thread(this::connect, "Algorithm-Connector");
        this.connectThread.setDaemon(false);
        this.connectThread.start();
    }

    private Metadata createMetadata() {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("algorithmId", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(this.properties.getId().getBytes(StandardCharsets.UTF_8))
        );
        metadata.put(
                Metadata.Key.of("algorithmName", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(this.properties.getName().getBytes(StandardCharsets.UTF_8))
        );
        metadata.put(
                Metadata.Key.of("serviceId", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(this.properties.getServiceId().getBytes(StandardCharsets.UTF_8))
        );
        return metadata;
    }

    private void connect() {
        long retryInterval = properties.getReconnectInterval().toMillis();

        logger.info("开始连接算法服务, 重试间隔: {}ms", retryInterval);

        String id = this.properties.getId();
        String name = this.properties.getName();
        String serviceId = this.properties.getServiceId();

        int retryTimes = 0;
        while (this.running.get()) {
            retryTimes++;

            logger.info("第 {} 次连接算法服务", retryTimes);

            try {
                logger.info("注册算法: id={}, name={}, serviceId={}", id, name, serviceId);


                ClientCall<SchemaResult, SchemaRequest> schemaCall = channel.newCall(
                        AlgorithmServiceGrpc.getSchemaStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );

                this.schemaHandler = new SchemaHandler(schemaCall, this.app);
                schemaCall.start(schemaHandler, this.createMetadata());
                schemaCall.request(Integer.MAX_VALUE);

                ClientCall<RunResult, RunRequest> call = channel.newCall(
                        AlgorithmServiceGrpc.getRunStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );

                this.handler = new AlgorithmHandler(call, this.app, this.functions, this.executor);
                call.start(handler, this.createMetadata());
                call.request(Integer.MAX_VALUE);
                
                logger.info("注册算法: 成功, id={}, name={}, serviceId={}", id, name, serviceId);

                // 开启心跳
                this.startHeartbeat();

                break;
            } catch (Exception e) {
                logger.error("第 {} 次连接算法服务失败", retryTimes, e);
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

        this.heartbeatThread = new Thread(this::heartbeat, "Algorithm-Heartbeat");
        this.heartbeatThread.start();
    }

    /**
     * 定时发送心跳任务
     */
    private void heartbeat() {
        long heartbeatInterval = this.properties.getKeepaliveInterval().toMillis();

        logger.info("心跳任务已启动, 心跳间隔 {}ms", heartbeatInterval);

        String id = this.properties.getId();
        String name = this.properties.getName();
        String serviceId = this.properties.getServiceId();
        int failureTimes = 0;
        while (this.running.get()) {
            try {
                TimeUnit.MILLISECONDS.sleep(heartbeatInterval);
            } catch (InterruptedException e) {
                logger.info("发送心跳任务被中断");
                return;
            }

            try {
                logger.info("发送心跳: id={}, name={}, serviceId={}", id, name, serviceId);

                HealthCheckResponse response = this.algorithmService.healthCheck(HealthCheckRequest
                        .newBuilder()
                        .setService(serviceId)
                        .build());

                logger.info("接收到心跳响应, id={}, name={}, serviceId={}, status={}", id, name, serviceId, response.getStatus());

                if (!response.getErrorsList().isEmpty()) {
                    for (Error error : response.getErrorsList()) {
                        logger.error("心跳响应错误, id={}, name={}, serviceId={}, code={}, message={}",
                                id, name, serviceId, error.getCode(), error.getMessage());
                    }
                }

                // 如果心跳响应不是 SERVING, 则表明服务出现问题, 重新连接
                if (!HealthCheckResponse.ServingStatus.SERVING.equals(response.getStatus())) {
                    logger.error("心跳检测到服务状态异常, 重新连接. id={}, name={}, serviceId={}, status={}", id, name, serviceId, response.getStatus());
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
        }
    }


    static class SchemaHandler extends ClientCall.Listener<SchemaRequest> {

        private final Logger logger = LoggerFactory.getLogger(SchemaHandler.class);

        private final Gson gson = new Gson();
        private final ClientCall<SchemaResult, SchemaRequest> call;
        private final AlgorithmApp app;

        public SchemaHandler(ClientCall<SchemaResult, SchemaRequest> call, AlgorithmApp app) {
            this.call = call;
            this.app = app;
        }

        public void close() {
            this.call.cancel("手动关闭", null);
        }

        @Override
        public void onMessage(SchemaRequest request) {
            String requestId = request.getRequest();

            Response response;
            try {
                String schema = this.app.schema();
                response = new Response(200, null, schema);
            } catch (Exception e) {
                logger.error("请求 schema 异常", e);
                response = new Response(400, e.getMessage());
            }

            this.call.sendMessage(SchemaResult.newBuilder()
                    .setRequest(requestId)
                    .setMessage(ByteString.copyFromUtf8(gson.toJson(response)))
                    .build());
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            logger.warn("算法程序 schema stream 已关闭");
        }

        @Override
        public void onReady() {
            logger.info("算法程序 schema stream 已就绪");
        }
    }
}
