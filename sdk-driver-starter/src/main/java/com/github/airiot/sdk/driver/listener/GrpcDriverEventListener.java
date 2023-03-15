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

package com.github.airiot.sdk.driver.listener;

import com.github.airiot.sdk.driver.DeviceInfo;
import com.github.airiot.sdk.driver.DriverApp;
import com.github.airiot.sdk.driver.GlobalContext;
import com.github.airiot.sdk.driver.config.BasicConfig;
import com.github.airiot.sdk.driver.config.Device;
import com.github.airiot.sdk.driver.config.DriverSingleConfig;
import com.github.airiot.sdk.driver.config.Model;
import com.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import com.github.airiot.sdk.driver.configuration.properties.DriverListenerProperties;
import com.github.airiot.sdk.driver.grpc.driver.Error;
import com.github.airiot.sdk.driver.grpc.driver.*;
import com.github.airiot.sdk.driver.model.Tag;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * 基于 GRPC 的驱动事件监听器实现
 * <br>
 * 通过 GRPC 监听平台下发到驱动的相关操作, 然后将平台下发操作转给具体的驱动实现类
 *
 * @see DriverApp 具体的驱动实现类
 */
public class GrpcDriverEventListener implements DriverEventListener {

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
    private Thread beatHeart;

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
     * 连接成功后, 创建相应的 stream
     */
    private void onConnected() {
        Channel channel = this.driverGrpcClient.getChannel();

        // start
        ClientCall<StartResult, StartRequest> startCall = channel.newCall(
                DriverServiceGrpc.getStartStreamMethod(),
                CallOptions.DEFAULT.withWaitForReady()
        );
        StartHandler startHandler = new StartHandler(startCall, this.driverApp, this.globalContext,
                this.getDriverConfigType(), this.getTagType(), this::handleStreamClosed);
        startCall.start(startHandler, metadata);
        startCall.request(Integer.MAX_VALUE);

        // schema
        ClientCall<SchemaResult, SchemaRequest> schemaCall = channel.newCall(
                DriverServiceGrpc.getSchemaStreamMethod(),
                CallOptions.DEFAULT.withWaitForReady()
        );
        SchemaHandler schemaHandler = new SchemaHandler(schemaCall, this.driverApp, this::handleStreamClosed);
        schemaCall.start(schemaHandler, this.metadata);
        schemaCall.request(Integer.MAX_VALUE);

        Type commandType = this.getCommandType();

        // run
        ClientCall<RunResult, RunRequest> runCall = channel.newCall(
                DriverServiceGrpc.getRunStreamMethod(),
                CallOptions.DEFAULT.withWaitForReady()
        );
        RunHandler runHandler = new RunHandler(runCall, this.driverApp, commandType, this::handleStreamClosed);
        runCall.start(runHandler, this.metadata);
        runCall.request(Integer.MAX_VALUE);

        // writeTag
        ClientCall<RunResult, RunRequest> writeTagCall = channel.newCall(
                DriverServiceGrpc.getWriteTagStreamMethod(),
                CallOptions.DEFAULT.withWaitForReady()
        );
        WriteTagHandler writeTagHandler = new WriteTagHandler(writeTagCall, this.driverApp, commandType, this::handleStreamClosed);
        writeTagCall.start(writeTagHandler, this.metadata);
        writeTagCall.request(Integer.MAX_VALUE);

        // batchRun
        ClientCall<BatchRunResult, BatchRunRequest> batchRunCall = channel.newCall(
                DriverServiceGrpc.getBatchRunStreamMethod(),
                CallOptions.DEFAULT.withWaitForReady()
        );
        BatchRunHandler batchRunHandler = new BatchRunHandler(batchRunCall, this.driverApp, commandType, this::handleStreamClosed);
        batchRunCall.start(batchRunHandler, this.metadata);
        batchRunCall.request(Integer.MAX_VALUE);

        // debug
        ClientCall<Debug, Debug> debugCall = channel.newCall(
                DriverServiceGrpc.getDebugStreamMethod(),
                CallOptions.DEFAULT.withWaitForReady()
        );
        DebugHandler debugHandler = new DebugHandler(debugCall, this.driverApp, this::handleStreamClosed);
        debugCall.start(debugHandler, this.metadata);
        debugCall.request(Integer.MAX_VALUE);
    }

    private void healthCheck() {
        long keepalive = this.grpcProperties.getKeepalive().toMillis();
        while (true) {
            if (State.CLOSED.equals(state.get()) || State.CLOSING.equals(state.get())) {
                break;
            }

            log.debug("心跳检测: 发送中");

            try {
                HealthCheckResponse response = this.driverGrpcClient.healthCheck(HealthCheckRequest.newBuilder()
                        .setService(this.driverInstanceId)
                        .build());
                log.debug("心跳检测: status = {}", response.getStatus());

                if (HealthCheckResponse.ServingStatus.SERVING.equals(response.getStatus())) {
                    if (state.get().isConnecting()) {
                        log.info("心跳检测: 连接成功, {} -> {}", state.get(), State.RUNNING);
                        this.onConnected();
                    }
                    state.set(State.RUNNING);
                    this.reconnecting.set(false);
                }

                List<Error> errors = response.getErrorsList();
                if (!CollectionUtils.isEmpty(errors)) {
                    for (Error error : errors) {
                        log.error("{}: {}", error.getCode(), error.getMessage());
                    }
                }
            } catch (StatusRuntimeException e) {
                log.error("心跳检测: 发送失败, {} -> {}", state.get(), State.RECONNECTING, e);
                state.set(State.RECONNECTING);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(keepalive);
            } catch (InterruptedException e) {
                log.info("心跳检测: interrupted");
                break;
            }
        }

        this.beatHeart = null;
    }

    @Override
    public void start() {
        if (!state.compareAndSet(State.CLOSED, State.CONNECTING)) {
            return;
        }

        // start health check
        this.beatHeart = new Thread(this::healthCheck);
        this.beatHeart.setName("healthCheck");
        this.beatHeart.setDaemon(false);
        this.beatHeart.start();
    }

    @Override
    public void stop() {
        if (state.get().equals(State.CLOSED)) {
            return;
        }
        state.set(State.CLOSING);

        // close driver
        this.driverApp.stop();

        state.set(State.CLOSED);
    }

    @Override
    public boolean isRunning() {
        return State.RUNNING.equals(state.get());
    }

    private void handleStreamClosed(Status status, Metadata trailers) {
        if (this.reconnecting.compareAndSet(false, true)) {
            log.warn("stream closed, reconnecting, status = {}, metadata = {}", status, trailers);
            this.state.set(State.RECONNECTING);
        }
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

        public StartHandler(ClientCall<StartResult, StartRequest> clientCall,
                            DriverApp<Object, Object, Object> driverApp,
                            GlobalContext globalContext,
                            Type driverConfigType, Type tagType,
                            StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.globalContext = globalContext;
            this.driverConfigType = driverConfigType;
            this.tagType = tagType;
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

            Gson gson = new Gson();
            boolean passed = true;
            try {
                Type baseConfigType = TypeToken.getParameterized(
                        DriverSingleConfig.class,
                        TypeToken.getParameterized(BasicConfig.class, this.tagType).getType()
                ).getType();
                DriverSingleConfig<BasicConfig<? extends Tag>> driverConfig = gson.fromJson(config, baseConfigType);

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
                    Object drvConfig = gson.fromJson(config, this.driverConfigType);
                    this.driverApp.start(drvConfig);
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
    }
}
