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

package io.github.airiot.sdk.driver.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.driver.DeviceInfo;
import io.github.airiot.sdk.driver.DriverApp;
import io.github.airiot.sdk.driver.GlobalContext;
import io.github.airiot.sdk.driver.config.BasicConfig;
import io.github.airiot.sdk.driver.config.Device;
import io.github.airiot.sdk.driver.config.DriverConfig;
import io.github.airiot.sdk.driver.config.Model;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverListenerProperties;
import io.github.airiot.sdk.driver.event.DriverReloadApplicationEvent;
import io.github.airiot.sdk.driver.grpc.driver.Error;
import io.github.airiot.sdk.driver.grpc.driver.*;
import io.github.airiot.sdk.driver.model.Tag;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 基于 GRPC 的驱动事件监听器实现
 * <br>
 * 通过 GRPC 监听平台下发到驱动的相关操作, 然后将平台下发操作转给具体的驱动实现类
 *
 * @see DriverApp 具体的驱动实现类
 */
public class GrpcDriverEventListener implements DriverEventListener, ApplicationContextAware {

    private final Logger log = LoggerFactory.getLogger(GrpcDriverEventListener.class);

    private static final Gson GSON = new Gson();

    private final String driverInstanceId;
    private final DriverListenerProperties grpcProperties;
    private final GlobalContext globalContext;
    private final DriverApp<Object, Object, Object> driverApp;
    private final DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient;

    private final Type[] parameterizedTypes;
    private final Metadata metadata;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    private ApplicationContext applicationContext;

    private Thread connectThread;
    private Thread healthCheckThread;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static <T> ByteString encode(T data) {
        if (data == null) {
            throw new NullPointerException();
        }
        return ByteString.copyFrom(GSON.toJson(data), StandardCharsets.UTF_8);
    }

    public GrpcDriverEventListener(DriverAppProperties driverProperties,
                                   DriverListenerProperties grpcProperties,
                                   GlobalContext globalContext,
                                   DriverApp<Object, Object, Object> driverApp,
                                   DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        this.globalContext = globalContext;
        this.driverApp = driverApp;
        this.grpcProperties = grpcProperties;

        this.driverInstanceId = driverProperties.getInstanceId();
        this.parameterizedTypes = this.parseParameterizedTypes();

        this.metadata = new Metadata();
        metadata.put(Metadata.Key.of("projectId", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(driverProperties.getProjectId().getBytes(StandardCharsets.UTF_8)));
        metadata.put(Metadata.Key.of("driverId", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(driverProperties.getId().getBytes(StandardCharsets.UTF_8)));
        metadata.put(Metadata.Key.of("driverName", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(driverProperties.getName().getBytes(StandardCharsets.UTF_8)));
        metadata.put(Metadata.Key.of("serviceId", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(this.driverInstanceId.getBytes(StandardCharsets.UTF_8)));

        ClientInterceptor metadataInterceptor = MetadataUtils.newAttachHeadersInterceptor(new Metadata());
        this.driverGrpcClient = driverGrpcClient.withInterceptors(metadataInterceptor);
    }

    private void clearTagValueCache(Void v) {
        // 发布驱动重载事件
        this.applicationContext.publishEvent(new DriverReloadApplicationEvent());
    }

    private Type[] parseParameterizedTypes() {
        for (Type type : this.driverApp.getClass().getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }

            ParameterizedType pType = (ParameterizedType) type;
            if (!((Class<?>) pType.getRawType()).isAssignableFrom(DriverApp.class)) {
                continue;
            }

            return pType.getActualTypeArguments();
        }

        throw new IllegalStateException("the type " + this.driverApp.getClass().getName() + " is not implements interface " + DriverApp.class.getName());
    }

    /**
     * 驱动配置的类型
     */
    private Type getDriverConfigType() {
        return this.parameterizedTypes[0];
    }

    /**
     * 驱动指令的类型
     */
    private Type getCommandType() {
        return this.parameterizedTypes[1];
    }

    /**
     * 数据点的类型
     */
    private Type getTagType() {
        return this.parameterizedTypes[2];
    }

    /**
     * 启动心跳检测
     */
    private void startHealthCheck() {
        if (this.healthCheckThread != null) {
            this.healthCheckThread.interrupt();
            this.healthCheckThread = null;
        }

        log.info("创建心跳检测线程");

        this.healthCheckThread = new Thread(this::healthCheck, "healthCheck");
//        this.healthCheckThread.setDaemon(true);
        this.healthCheckThread.start();
    }

    private void healthCheck() {
        long keepalive = this.grpcProperties.getKeepalive().toMillis();
        log.info("心跳检测已启动, 心跳间隔 {}ms", keepalive);
        while (State.RUNNING.equals(this.state.get())) {
            try {
                TimeUnit.MILLISECONDS.sleep(keepalive);
            } catch (InterruptedException e) {
                log.info("心跳检测: 被终止");
                return;
            }

            log.info("心跳检测: 发送心跳");

            try {
                HealthCheckResponse response = this.driverGrpcClient.healthCheck(HealthCheckRequest.newBuilder()
                        .setService(this.driverInstanceId)
                        .build());
                log.info("心跳检测: 接收到心跳响应, status = {}", response.getStatus());

                List<Error> errors = response.getErrorsList();
                if (!CollectionUtils.isEmpty(errors)) {
                    for (Error error : errors) {
                        log.error("心跳检测: 接收到错误信息, code = {}, message = {}", error.getCode(), error.getMessage());
                    }
                }

                if (!HealthCheckResponse.ServingStatus.SERVING.equals(response.getStatus())) {
                    log.error("心跳检测: 响应状态不是 SERVING, 重新连接, status = {}", response.getStatus());
                    break;
                }
            } catch (StatusRuntimeException e) {
                log.error("心跳检测: 心跳检测异常", e);
                break;
            }
        }

        if (this.state.get().isRunning()) {
            log.info("重新连接 Driver 服务");
            this.state.set(State.RECONNECTING);
            this.connect();
        }
    }

    @Override
    public void start() {
        if (!state.compareAndSet(State.CLOSED, State.CONNECTING)) {
            return;
        }

        this.connect();
    }

    @Override
    public void stop() {
        if (!state.get().isRunning()) {
            log.info("驱动已停止");
            return;
        }

        log.info("停止驱动");

        state.set(State.CLOSING);


        if (this.healthCheckThread != null) {
            this.healthCheckThread.interrupt();
            this.healthCheckThread = null;
        }

        if (this.connectThread != null) {
            this.connectThread.interrupt();
            this.connectThread = null;
        }

        try {
            // close driver
            this.driverApp.stop();
        } catch (Exception e) {
            log.warn("停止驱动异常", e);
        }

        state.set(State.CLOSED);
        log.info("驱动已停止");
    }

    /**
     * 连接 Driver 服务
     */
    private void connect() {
        if (this.connectThread != null) {
            this.connectThread.interrupt();
            this.connectThread = null;
        }

        log.info("创建连接 Driver 服务线程");
        this.connectThread = new Thread(this::connectTask);
        this.connectThread.setName("connectTask");
//        this.connectThread.setDaemon(true);
        this.connectThread.start();
    }

    /**
     * 连接平台
     */
    private void connectTask() {
        Channel channel = this.driverGrpcClient.getChannel();
        int retryTimes = 0;
        long retryInterval = this.grpcProperties.getReconnectInterval().toMillis();

        log.info("连接 Driver 服务线程已启动, 重连间隔 {}ms", retryInterval);

        while (this.state.get().isRunning()) {
            retryTimes++;

            log.info("连接 Driver 服务: 第 {} 次连接", retryTimes);

            try {
                // schema
                ClientCall<SchemaResult, SchemaRequest> schemaCall = channel.newCall(
                        DriverServiceGrpc.getSchemaStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata schemaMetadata = new Metadata();
                schemaMetadata.merge(this.metadata);
                SchemaHandler schemaHandler = new SchemaHandler(schemaCall, this.driverApp, this::handleStreamClosed);
                schemaCall.start(schemaHandler, schemaMetadata);
                schemaCall.request(Integer.MAX_VALUE);

                Type commandType = this.getCommandType();

                // run
                ClientCall<RunResult, RunRequest> runCall = channel.newCall(
                        DriverServiceGrpc.getRunStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata runMetadata = new Metadata();
                runMetadata.merge(this.metadata);
                RunHandler runHandler = new RunHandler(runCall, this.driverApp, commandType, this::handleStreamClosed);
                runCall.start(runHandler, runMetadata);
                runCall.request(Integer.MAX_VALUE);

                // writeTag
                ClientCall<RunResult, RunRequest> writeTagCall = channel.newCall(
                        DriverServiceGrpc.getWriteTagStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata writeTagMetadata = new Metadata();
                writeTagMetadata.merge(this.metadata);
                WriteTagHandler writeTagHandler = new WriteTagHandler(writeTagCall, this.driverApp, commandType, this::handleStreamClosed);
                writeTagCall.start(writeTagHandler, writeTagMetadata);
                writeTagCall.request(Integer.MAX_VALUE);

                // batchRun
                ClientCall<BatchRunResult, BatchRunRequest> batchRunCall = channel.newCall(
                        DriverServiceGrpc.getBatchRunStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata batchRunMetadata = new Metadata();
                batchRunMetadata.merge(this.metadata);
                BatchRunHandler batchRunHandler = new BatchRunHandler(batchRunCall, this.driverApp, commandType, this::handleStreamClosed);
                batchRunCall.start(batchRunHandler, batchRunMetadata);
                batchRunCall.request(Integer.MAX_VALUE);

                // debug
                ClientCall<Debug, Debug> debugCall = channel.newCall(
                        DriverServiceGrpc.getDebugStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata debugRunMetadata = new Metadata();
                debugRunMetadata.merge(this.metadata);
                DebugHandler debugHandler = new DebugHandler(debugCall, this.driverApp, this::handleStreamClosed);
                debugCall.start(debugHandler, debugRunMetadata);
                debugCall.request(Integer.MAX_VALUE);

                // start
                ClientCall<StartResult, StartRequest> startCall = channel.newCall(
                        DriverServiceGrpc.getStartStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata startMetadata = new Metadata();
                startMetadata.merge(this.metadata);
                StartHandler startHandler = new StartHandler(startCall, this.driverApp, this.globalContext,
                        this.getDriverConfigType(), this.getTagType(),
                        this::handleStreamClosed, this::clearTagValueCache);
                startCall.start(startHandler, startMetadata);
                startCall.request(Integer.MAX_VALUE);

                this.state.set(State.RUNNING);

                log.info("连接 Driver 服务: 第 {} 次连接成功", retryTimes);

                break;
            } catch (Exception e) {
                log.error("连接 Driver 服务: 第 {} 次连接失败", retryTimes, e);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(retryInterval);
            } catch (InterruptedException e) {
                log.info("连接 Driver 服务: 被终止");
                return;
            }
        }

        if (State.RUNNING.equals(this.state.get())) {
            this.startHealthCheck();
        }
    }

    @Override
    public boolean isRunning() {
        return State.RUNNING.equals(state.get());
    }

    private void handleStreamClosed(Status status, Metadata trailers) {
//        if (this.reconnecting.compareAndSet(false, true)) {
//            log.warn("stream closed, reconnecting, status = {}, metadata = {}", status, trailers);
//            this.state.set(State.CONNECTING);
//        }
    }

    static class RunHandler extends ClientCall.Listener<RunRequest> {
        private final Logger log = LoggerFactory.getLogger("run-stream");

        private final ClientCall<RunResult, RunRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final Type commandType;
        private final StreamClosedCallback closedCallback;

        public RunHandler(ClientCall<RunResult, RunRequest> clientCall,
                          DriverApp<Object, Object, Object> driverApp,
                          Type commandType, StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.commandType = commandType;
            this.closedCallback = closedCallback;
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            this.closedCallback.handle(status, trailers);
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(RunRequest request) {
            String req = request.getRequest();
            String serialNo = request.getSerialNo();

            log.info("下发指令, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

            Result result = new Result();
            result.setCode(200);

            try {
                Object command = new Gson().fromJson(request.getCommand().toStringUtf8(), this.commandType);
                Cmd<Object> cmd = new Cmd<>(req, request.getTableId(), request.getId(), serialNo, command);
                Object runResult = this.driverApp.run(cmd);
                result.setResult(runResult);
                log.info("下发指令, 成功, req = {}, serialNo = {}, command = {}, result = {}",
                        req, serialNo, request.getCommand().toStringUtf8(), runResult);
            } catch (JsonSyntaxException e) {
                log.error("下发指令, 解析命令失败, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                result.setResult(500);
                result.setResult(e.getMessage());
            } catch (Exception e) {
                log.error("下发指令, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                result.setResult(500);
                result.setResult(e.getMessage());
            }

            try {
                clientCall.sendMessage(RunResult.newBuilder()
                        .setRequest(req)
                        .setMessage(GrpcDriverEventListener.encode(result))
                        .build());
            } catch (Exception e) {
                log.error("上报指令下发结果失败, req = {}, serialNo = {}, command = {}",
                        req, serialNo, request.getCommand().toStringUtf8(), e);
            }
        }
    }

    static class WriteTagHandler extends ClientCall.Listener<RunRequest> {
        private final Logger log = LoggerFactory.getLogger("write-tag-stream");
        private final ClientCall<RunResult, RunRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final Type commandType;
        private final StreamClosedCallback closedCallback;

        public WriteTagHandler(ClientCall<RunResult, RunRequest> clientCall,
                               DriverApp<Object, Object, Object> driverApp,
                               Type commandType, StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.commandType = commandType;
            this.closedCallback = closedCallback;
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            this.closedCallback.handle(status, trailers);
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(RunRequest request) {
            String req = request.getRequest();
            String serialNo = request.getSerialNo();

            log.info("写数据点, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

            Result result = new Result();
            result.setCode(200);

            try {
                Object command = new Gson().fromJson(request.getCommand().toStringUtf8(), this.commandType);
                Cmd<Object> cmd = new Cmd<>(req, request.getTableId(), request.getId(), serialNo, command);
                Object runResult = this.driverApp.writeTag(cmd);
                result.setResult(runResult);
                log.info("写数据点, 成功, req = {}, serialNo = {}, command = {}, result = {}",
                        req, serialNo, request.getCommand().toStringUtf8(), runResult);
            } catch (JsonSyntaxException e) {
                log.error("写数据点, 解析命令失败, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                result.setResult(500);
                result.setResult(e.getMessage());
            } catch (Exception e) {
                log.error("写数据点, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                result.setResult(500);
                result.setResult(e.getMessage());
            }

            try {
                clientCall.sendMessage(RunResult.newBuilder()
                        .setRequest(req)
                        .setMessage(GrpcDriverEventListener.encode(result))
                        .build());
            } catch (Exception e) {
                log.error("上报写数据点结果失败, req = {}, serialNo = {}, command = {}",
                        req, serialNo, request.getCommand().toStringUtf8(), e);
            }
        }
    }

    static class BatchRunHandler extends ClientCall.Listener<BatchRunRequest> {
        private final Logger log = LoggerFactory.getLogger("batch-run-stream");
        private final ClientCall<BatchRunResult, BatchRunRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final Type commandType;
        private final StreamClosedCallback closedCallback;

        public BatchRunHandler(ClientCall<BatchRunResult, BatchRunRequest> clientCall,
                               DriverApp<Object, Object, Object> driverApp,
                               Type commandType, StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.commandType = commandType;
            this.closedCallback = closedCallback;
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            this.closedCallback.handle(status, trailers);
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(BatchRunRequest request) {
            String req = request.getRequest();
            String serialNo = request.getSerialNo();

            log.info("批量下发指令, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

            Result result = new Result();
            result.setCode(200);

            try {
                Object command = new Gson().fromJson(request.getCommand().toStringUtf8(), this.commandType);
                BatchCmd<Object> cmd = new BatchCmd<>(req, request.getTableId(), request.getIdList(), serialNo, command);
                Object runResult = this.driverApp.batchRun(cmd);
                result.setResult(runResult);
                log.info("批量下发指令, 成功, req = {}, serialNo = {}, command = {}, result = {}",
                        req, serialNo, request.getCommand().toStringUtf8(), runResult);
            } catch (JsonSyntaxException e) {
                log.error("批量下发指令, 解析命令失败, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                result.setResult(500);
                result.setResult(e.getMessage());
            } catch (Exception e) {
                log.error("批量下发指令, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                result.setResult(500);
                result.setResult(e.getMessage());
            }

            try {
                clientCall.sendMessage(BatchRunResult.newBuilder()
                        .setRequest(req)
                        .setMessage(GrpcDriverEventListener.encode(result))
                        .build());
            } catch (Exception e) {
                log.error("上报批量下发指令结果失败, req = {}, serialNo = {}, command = {}",
                        req, serialNo, request.getCommand().toStringUtf8(), e);
            }
        }
    }

    static class DebugHandler extends ClientCall.Listener<Debug> {
        private final Logger log = LoggerFactory.getLogger("debug-stream");
        private final ClientCall<Debug, Debug> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final StreamClosedCallback closedCallback;

        public DebugHandler(ClientCall<Debug, Debug> clientCall,
                            DriverApp<Object, Object, Object> driverApp,
                            StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.closedCallback = closedCallback;
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            this.closedCallback.handle(status, trailers);
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(Debug request) {
            String req = request.getRequest();
            log.info("debug, req = {}", req);
            Debug debug;
            try {
                debug = this.driverApp.debug(request);
                if (log.isDebugEnabled()) {
                    log.debug("debug, req = {}, result = {}", req, debug);
                }

                if (debug == null) {
                    log.warn("debug, req = {}, 无返回值", req);
                    return;
                }

                debug = debug.toBuilder().setRequest(req).build();
            } catch (Exception e) {
                log.error("debug, req = {}", req, e);
                return;
            }

            try {
                clientCall.sendMessage(debug);
            } catch (Exception e) {
                log.error("上报 debug 结果失败, req = {}, result = {}", req, debug, e);
            }
        }
    }

    static class StartHandler extends ClientCall.Listener<StartRequest> {
        private final Logger log = LoggerFactory.getLogger("start-stream");
        private final ClientCall<StartResult, StartRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final GlobalContext globalContext;
        private final Type driverConfigType;
        private final Type tagType;
        private final StreamClosedCallback closedCallback;
        private final Consumer<Void> clearCacheFn;

        public StartHandler(ClientCall<StartResult, StartRequest> clientCall,
                            DriverApp<Object, Object, Object> driverApp,
                            GlobalContext globalContext,
                            Type driverConfigType, Type tagType,
                            StreamClosedCallback closedCallback,
                            Consumer<Void> clearCacheFn
        ) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.globalContext = globalContext;
            this.driverConfigType = driverConfigType;
            this.tagType = tagType;
            this.closedCallback = closedCallback;
            this.clearCacheFn = clearCacheFn;
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            this.closedCallback.handle(status, trailers);
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(StartRequest message) {
            String req = message.getRequest();
            String config = message.getConfig().toString(StandardCharsets.UTF_8);

            log.info("启动驱动, req = {}", req);
            if (log.isDebugEnabled()) {
                log.debug("启动驱动, req = {}, config  {}", req, config);
            }

            Result result = new Result();
            result.setCode(200);
            result.setResult("启动成功");

            boolean passed = true;
            try {
                Type baseConfigType = TypeReference.parametricType(BasicConfig.class, this.tagType);
                Type driverConfigType = TypeReference.parametricType(DriverConfig.class, baseConfigType, baseConfigType, baseConfigType);

                DriverConfig<BasicConfig<? extends Tag>, BasicConfig<? extends Tag>, BasicConfig<? extends Tag>> driverConfig = JSON.parseObject(config, driverConfigType);

                if (log.isDebugEnabled()) {
                    log.debug("启动驱动, config = {}", driverConfig);
                }

                String instanceId = driverConfig.getId();
                Map<String, List<DeviceInfo<? extends Tag>>> deviceInfos = new HashMap<>();

                Map<String, Tag> driverInstanceTags = new HashMap<>();
                if (driverConfig.getConfig() != null && !CollectionUtils.isEmpty(driverConfig.getConfig().getTags())) {
                    for (Tag tag : driverConfig.getConfig().getTags()) {
                        driverInstanceTags.put(tag.getId(), tag);
                    }
                }

                for (Model<BasicConfig<? extends Tag>, BasicConfig<? extends Tag>> table : driverConfig.getTables()) {
                    String tableId = table.getId();
                    table.setDriverInstanceId(instanceId);
                    Map<String, Tag> tableTags = new HashMap<>(driverInstanceTags);
                    if (table.getConfig() != null && !CollectionUtils.isEmpty(table.getConfig().getTags())) {
                        for (Tag tag : table.getConfig().getTags()) {
                            tableTags.put(tag.getId(), tag);
                        }
                    }

                    for (Device<BasicConfig<? extends Tag>> device : table.getDevices()) {
                        device.setDriverInstanceId(instanceId);
                        device.setTable(tableId);

                        Map<String, Tag> deviceTags = new HashMap<>(tableTags);
                        if (device.getConfig() != null && !CollectionUtils.isEmpty(device.getConfig().getTags())) {
                            for (Tag tag : device.getConfig().getTags()) {
                                deviceTags.put(tag.getId(), tag);
                            }
                        }
                        String deviceId = device.getId();
                        DeviceInfo<? extends Tag> info = new DeviceInfo<>(deviceId, tableId, instanceId, deviceTags);
                        deviceInfos.putIfAbsent(deviceId, new ArrayList<>(1));
                        deviceInfos.get(deviceId).add(info);
                    }
                }

                this.globalContext.set(deviceInfos);
            } catch (Exception e) {
                log.error("启动驱动, 解析启动配置失败, config = {}", config, e);
                passed = false;
                result.setResult(400);
                result.setResult("启动配置不正确: " + e.getMessage());
            }

            if (passed) {
                try {
                    Object drvConfig = JSON.parseObject(config, this.driverConfigType);
                    this.driverApp.start(drvConfig);
                    this.clearCacheFn.accept(null);
                } catch (Exception e) {
                    log.error("启动驱动:", e);
                    result.setCode(400);
                    result.setResult("启动失败: " + e.getMessage());
                }
            }

            clientCall.sendMessage(StartResult.newBuilder()
                    .setRequest(message.getRequest())
                    .setMessage(GrpcDriverEventListener.encode(result))
                    .build());
        }
    }

    static class SchemaHandler extends ClientCall.Listener<SchemaRequest> {

        private final Logger log = LoggerFactory.getLogger("schema-stream");

        private final ClientCall<SchemaResult, SchemaRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final StreamClosedCallback closedCallback;

        public SchemaHandler(ClientCall<SchemaResult, SchemaRequest> clientCall,
                             DriverApp<Object, Object, Object> driverApp,
                             StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.closedCallback = closedCallback;
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            this.closedCallback.handle(status, trailers);
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(SchemaRequest request) {
            log.info("req = {}, type = schema", request.getRequest());
            Result result = new Result();
            try {
                String schema = this.driverApp.schema();
                if (log.isDebugEnabled()) {
                    log.debug("req = {}, type = schema, {}", request.getRequest(), schema);
                }

                // 替换版本号
                schema = schema.replaceAll("__version__", driverApp.getVersion());
                schema = schema.replaceAll("__sdk_version__", GlobalContext.getVersion());

                result.setCode(200);
                result.setResult(schema);
            } catch (Exception e) {
                log.error("req = {}, type = schema", request.getRequest(), e);
                result.setCode(500);
                result.setResult(e.getMessage());
            }

            String message = new Gson().toJson(result);
            clientCall.sendMessage(SchemaResult.newBuilder()
                    .setRequest(request.getRequest())
                    .setMessage(ByteString.copyFrom(message, StandardCharsets.UTF_8))
                    .build());
        }
    }

    @FunctionalInterface
    interface StreamClosedCallback {
        void handle(Status status, Metadata trailers);
    }

    public enum State {
        CLOSED,
        CONNECTING,
        RECONNECTING,
        RUNNING,
        CLOSING;

        public boolean isConnecting() {
            return State.CONNECTING.equals(this) || State.RECONNECTING.equals(this);
        }

        public boolean isRunning() {
            return !State.CLOSING.equals(this) && !State.CLOSED.equals(this);
        }
    }
}
