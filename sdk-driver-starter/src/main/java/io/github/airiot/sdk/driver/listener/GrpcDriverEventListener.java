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

import ch.qos.logback.classic.Level;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.driver.DeviceInfo;
import io.github.airiot.sdk.driver.DriverApp;
import io.github.airiot.sdk.driver.DriverModules;
import io.github.airiot.sdk.driver.GlobalContext;
import io.github.airiot.sdk.driver.config.BasicConfig;
import io.github.airiot.sdk.driver.config.Device;
import io.github.airiot.sdk.driver.config.DriverSingleConfig;
import io.github.airiot.sdk.driver.config.Model;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverListenerProperties;
import io.github.airiot.sdk.driver.event.DriverReloadApplicationEvent;
import io.github.airiot.sdk.driver.grpc.driver.Error;
import io.github.airiot.sdk.driver.grpc.driver.*;
import io.github.airiot.sdk.driver.model.Tag;
import io.github.airiot.sdk.logger.LoggerContext;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.*;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
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

    private final Logger log = LoggerFactory.withContext().module(DriverModules.START).getStaticLogger(GrpcDriverEventListener.class);

    private final Logger healthCheckLogger = LoggerFactory.withContext().module(DriverModules.HEARTBEAT).getStaticLogger(GrpcDriverEventListener.class);

    private static final String STREAM_HEARTBEAT = "heartbeat";

    private static final Gson GSON = new Gson();

    private final String projectId;
    private final String driverId;
    private final String driverInstanceId;
    private final DriverListenerProperties grpcProperties;
    private final GlobalContext globalContext;
    private final DriverApp<Object, Object, Object> driverApp;
    private final DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient;

    private final Type[] parameterizedTypes;
    private final Metadata metadata;

    private final ThreadPoolExecutor runExecutor;
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);

    private final Map<String, Level> loggerRoots = new HashMap<>();
    private ApplicationContext applicationContext;

    private ClientCall<SchemaResult, SchemaRequest> schemaCall = null;
    private ClientCall<RunResult, RunRequest> runCall = null;
    private ClientCall<RunResult, RunRequest> writeTagCall = null;
    private ClientCall<BatchRunResult, BatchRunRequest> batchRunCall = null;
    private ClientCall<Debug, Debug> debugCall = null;
    private ClientCall<StartResult, StartRequest> startCall = null;
    private ClientCall<HttpProxyResult, HttpProxyRequest> httpProxyCall = null;

    private StartHandler startHandler;
    private RunHandler runHandler;
    private WriteTagHandler writeTagHandler;
    private BatchRunHandler batchRunHandler;
    private DebugHandler debugHandler;
    private SchemaHandler schemaHandler;
    private HttpProxyHandler httpProxyHandler;

    /**
     * 上次连接时间
     */
    private volatile long lastConnectTime = 0;
    private Thread connectThread;
    private Thread healthCheckThread;
    private Thread streamHeartbeatThread;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Map<String, Object> springApplications = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        if (!CollectionUtils.isEmpty(springApplications)) {
            // 找出所有的日志配置, 即 logging.level 下的配置
            Environment environment = applicationContext.getEnvironment();
            if (environment instanceof ConfigurableEnvironment) {
                String prefix = "logging.level.";
                int prefixLength = prefix.length();
                MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
                for (PropertySource<?> propertySource : propertySources) {
                    if (!(propertySource instanceof EnumerablePropertySource)) {
                        continue;
                    }
                    String[] propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();
                    for (String propertyName : propertyNames) {
                        if (propertyName.startsWith(prefix)) {
                            loggerRoots.putIfAbsent(propertyName.substring(prefixLength), Level.toLevel(String.valueOf(propertySource.getProperty(propertyName)), Level.INFO));
                        }
                    }
                }
            }
            loggerRoots.putIfAbsent("io.github.airiot.sdk", Level.INFO);
            for (Map.Entry<String, Object> entry : springApplications.entrySet()) {
                if (entry.getValue() != null) {
                    loggerRoots.putIfAbsent(entry.getValue().getClass().getPackage().getName(), Level.INFO);
                }
            }
        }
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

        this.projectId = driverProperties.getProjectId();
        this.driverId = driverProperties.getId();
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

        // 创建指令执行线程池
        int cpus = Runtime.getRuntime().availableProcessors();
        int corePoolSize = cpus / 2 + 1;
        int maxPoolSize = cpus;
        if (grpcProperties.getRunMaxThreads() != 0) {
            corePoolSize = grpcProperties.getRunMaxThreads();
            maxPoolSize = corePoolSize;
        }

        int queueSize = grpcProperties.getRunQueueSize();
        queueSize = queueSize <= 0 ? 32 : queueSize;
        this.runExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private void clearTagValueCache(DriverSingleConfig<BasicConfig<?>> driverConfigs) {
        // 发布驱动重载事件
        this.applicationContext.publishEvent(new DriverReloadApplicationEvent(driverConfigs));
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

        healthCheckLogger.info("创建心跳检测线程");

        this.healthCheckThread = new Thread(this::healthCheck, "healthCheck");
        this.healthCheckThread.start();
    }

    /**
     * 启动流心跳检测
     */
    private void startStreamHealthCheck() {
        if (this.streamHeartbeatThread != null) {
            this.streamHeartbeatThread.interrupt();
        }

        healthCheckLogger.info("创建流心跳检测线程");

        this.streamHeartbeatThread = new Thread(this::streamHealthCheck, "healthCheck-stream");
        this.streamHeartbeatThread.start();
    }

    private void healthCheck() {
        long keepalive = this.grpcProperties.getKeepalive().toMillis();
        healthCheckLogger.info("心跳检测已启动, 心跳间隔 {}ms", keepalive);
        while (State.RUNNING.equals(this.state.get())) {
            try {
                TimeUnit.MILLISECONDS.sleep(keepalive);
            } catch (InterruptedException e) {
                healthCheckLogger.info("心跳检测: 被终止");
                return;
            }

            if (!State.RUNNING.equals(this.state.get())) {
                healthCheckLogger.info("心跳检测: 被终止");
                return;
            }

            healthCheckLogger.info("心跳检测: 发送心跳");

            try {
                HealthCheckResponse response = this.driverGrpcClient.healthCheck(HealthCheckRequest.newBuilder()
                        .setProjectId(this.projectId)
                        .setDriverId(this.driverId)
                        .setService(this.driverInstanceId)
                        .build());
                healthCheckLogger.info("心跳检测: 接收到心跳响应, status = {}", response.getStatus());

                List<Error> errors = response.getErrorsList();
                if (!CollectionUtils.isEmpty(errors)) {
                    for (Error error : errors) {
                        healthCheckLogger.error("心跳检测: 接收到错误信息, code = {}, message = {}", error.getCode(), error.getMessage());
                    }
                }

                if (!HealthCheckResponse.ServingStatus.SERVING.equals(response.getStatus())) {
                    healthCheckLogger.error("心跳检测: 响应状态不是 SERVING, 重新连接, status = {}", response.getStatus());
                    break;
                }
            } catch (StatusRuntimeException e) {
                healthCheckLogger.error("心跳检测: 心跳检测异常", e);
                break;
            }
        }

        if (this.state.get().isRunning()) {
            healthCheckLogger.info("重新连接 Driver 服务");
            this.state.set(State.RECONNECTING);
            this.connect();
        }
    }

    private void streamHealthCheck() {
        long keepalive = this.grpcProperties.getKeepalive().toMillis();
        long keepaliveTimeout = keepalive * 3;
        healthCheckLogger.info("流心跳检测已启动, 心跳间隔 {}ms", keepalive);

        try {
            TimeUnit.MILLISECONDS.sleep(Math.min((int) (keepalive * 0.25), 1000));
        } catch (InterruptedException e) {
            healthCheckLogger.info("流心跳检测被中断, 未开始");
            return;
        }

        while (State.RUNNING.equals(this.state.get())) {
            try {
                TimeUnit.MILLISECONDS.sleep(keepalive);
            } catch (InterruptedException e) {
                healthCheckLogger.info("流心跳检测: 被终止");
                return;
            }

            if (!State.RUNNING.equals(this.state.get())) {
                healthCheckLogger.info("流心跳检测: 被终止");
                return;
            }

            healthCheckLogger.info("流心跳检测: 发送心跳");

            try {
                if (this.startHandler != null) {
                    this.startHandler.heartbeat(keepaliveTimeout);
                }

                if (this.runHandler != null) {
                    this.runHandler.heartbeat(keepaliveTimeout);
                }

                if (this.batchRunHandler != null) {
                    this.batchRunHandler.heartbeat(keepaliveTimeout);
                }

                if (this.writeTagHandler != null) {
                    this.writeTagHandler.heartbeat(keepaliveTimeout);
                }

                if (this.schemaHandler != null) {
                    this.schemaHandler.heartbeat(keepaliveTimeout);
                }

                if (this.debugHandler != null) {
                    this.debugHandler.heartbeat(keepaliveTimeout);
                }

                if (this.driverApp.supportHttpProxy() && this.httpProxyHandler != null) {
                    this.httpProxyHandler.heartbeat(keepaliveTimeout);
                }
            } catch (Exception e) {
                healthCheckLogger.error("流心跳检测: 心跳检测异常", e);
                break;
            }
        }

        if (this.state.get().isRunning()) {
            healthCheckLogger.info("重新连接 Driver 服务");
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

        if (this.streamHeartbeatThread != null) {
            this.streamHeartbeatThread.interrupt();
            this.streamHeartbeatThread = null;
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

        if (this.schemaCall != null) {
            this.schemaCall.cancel("重新连接", null);
        }
        if (this.runCall != null) {
            this.runCall.cancel("重新连接", null);
        }
        if (this.writeTagCall != null) {
            this.writeTagCall.cancel("重新连接", null);
        }
        if (this.batchRunCall != null) {
            this.batchRunCall.cancel("重新连接", null);
        }
        if (this.debugCall != null) {
            this.debugCall.cancel("重新连接", null);
        }
        if (this.startCall != null) {
            this.startCall.cancel("重新连接", null);
        }
        if (this.httpProxyCall != null) {
            this.httpProxyCall.cancel("重新连接", null);
        }

        log.info("创建连接 Driver 服务线程");
        this.connectThread = new Thread(this::connectTask);
        this.connectThread.setName("connectTask");
        this.connectThread.start();
    }

    /**
     * 连接平台
     */
    private void connectTask() {
        Channel channel = this.driverGrpcClient.getChannel();
        int retryTimes = 0;
        long retryInterval = this.grpcProperties.getReconnectInterval().toMillis();

        if (this.lastConnectTime != 0) {
            long waitTime = retryInterval - (System.currentTimeMillis() - this.lastConnectTime);
            if (waitTime > 0) {
                try {
                    log.info("连接 Driver 服务: 等待 {}ms", waitTime);
                    TimeUnit.MILLISECONDS.sleep(waitTime);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        this.lastConnectTime = System.currentTimeMillis();

        log.info("连接 Driver 服务线程已启动, 重连间隔 {}ms", retryInterval);

        while (this.state.get().isRunning()) {
            retryTimes++;

            log.info("连接 Driver 服务: 第 {} 次连接", retryTimes);

            StreamClosedCallback callback = new OnceStreamClosedCallback(this::handleStreamClosed);

            String sessionId = UUID.randomUUID().toString();
            metadata.put(Metadata.Key.of("sessionId", Metadata.ASCII_STRING_MARSHALLER),
                    Hex.encodeHexString(sessionId.getBytes(StandardCharsets.UTF_8)));

            try {
                // schema
                this.schemaCall = channel.newCall(
                        DriverServiceGrpc.getSchemaStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata schemaMetadata = new Metadata();
                schemaMetadata.merge(this.metadata);

                this.schemaHandler = new SchemaHandler(this.schemaCall, this.driverApp, callback);
                this.schemaCall.start(schemaHandler, schemaMetadata);
                this.schemaCall.request(Integer.MAX_VALUE);

                Type commandType = this.getCommandType();

                // run
                this.runCall = channel.newCall(
                        DriverServiceGrpc.getRunStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata runMetadata = new Metadata();
                runMetadata.merge(this.metadata);
                this.runHandler = new RunHandler(this.runExecutor, this.runCall, this.driverApp, commandType, callback);
                this.runCall.start(runHandler, runMetadata);
                this.runCall.request(Integer.MAX_VALUE);

                // writeTag
                this.writeTagCall = channel.newCall(
                        DriverServiceGrpc.getWriteTagStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata writeTagMetadata = new Metadata();
                writeTagMetadata.merge(this.metadata);
                this.writeTagHandler = new WriteTagHandler(this.runExecutor, this.writeTagCall, this.driverApp, commandType, callback);
                this.writeTagCall.start(writeTagHandler, writeTagMetadata);
                this.writeTagCall.request(Integer.MAX_VALUE);

                // batchRun
                this.batchRunCall = channel.newCall(
                        DriverServiceGrpc.getBatchRunStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata batchRunMetadata = new Metadata();
                batchRunMetadata.merge(this.metadata);
                this.batchRunHandler = new BatchRunHandler(this.runExecutor, this.batchRunCall, this.driverApp, commandType, callback);
                this.batchRunCall.start(batchRunHandler, batchRunMetadata);
                this.batchRunCall.request(Integer.MAX_VALUE);

                // debug
                this.debugCall = channel.newCall(
                        DriverServiceGrpc.getDebugStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata debugRunMetadata = new Metadata();
                debugRunMetadata.merge(this.metadata);
                this.debugHandler = new DebugHandler(this.debugCall, this.driverApp, callback);
                this.debugCall.start(debugHandler, debugRunMetadata);
                this.debugCall.request(Integer.MAX_VALUE);

                // start
                this.startCall = channel.newCall(
                        DriverServiceGrpc.getStartStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata startMetadata = new Metadata();
                startMetadata.merge(this.metadata);
                this.startHandler = new StartHandler(this.startCall, this.driverApp, this.globalContext,
                        this.getDriverConfigType(), this.getTagType(),
                        callback, this.loggerRoots, this::clearTagValueCache);
                this.startCall.start(startHandler, startMetadata);
                this.startCall.request(Integer.MAX_VALUE);

                // httpProxy
                if (this.driverApp.supportHttpProxy()) {
                    this.httpProxyCall = channel.newCall(
                            DriverServiceGrpc.getHttpProxyStreamMethod(),
                            CallOptions.DEFAULT.withWaitForReady()
                    );
                    Metadata httpProxyMetadata = new Metadata();
                    httpProxyMetadata.merge(this.metadata);
                    this.httpProxyHandler = new HttpProxyHandler(this.httpProxyCall, this.driverApp, callback);
                    this.httpProxyCall.start(httpProxyHandler, httpProxyMetadata);
                    this.httpProxyCall.request(Integer.MAX_VALUE);
                }

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
            // 全局心跳
            this.startHealthCheck();
            // 流心跳
            this.startStreamHealthCheck();
        }
    }

    @Override
    public boolean isRunning() {
        return State.RUNNING.equals(state.get());
    }

    private void handleStreamClosed(Status status, Metadata trailers) {
        log.warn("stream closed, reconnecting, status = {}, metadata = {}", status, trailers);
        if (State.RUNNING.equals(this.state.get())) {
            this.state.set(State.RECONNECTING);
            this.connect();
        }
    }

    static class RunHandler extends ClientCall.Listener<RunRequest> {
        private final Logger log = LoggerFactory.withContext().module(DriverModules.START).getStaticLogger("run-stream");

        private final ThreadPoolExecutor executor;
        private final ClientCall<RunResult, RunRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final Type commandType;
        private final StreamClosedCallback closedCallback;

        // 心跳
        private CompletableFuture<Long> heartbeatFuture;

        public RunHandler(ThreadPoolExecutor executor, ClientCall<RunResult, RunRequest> clientCall,
                          DriverApp<Object, Object, Object> driverApp,
                          Type commandType, StreamClosedCallback closedCallback) {
            this.executor = executor;
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.commandType = commandType;
            this.closedCallback = closedCallback;
        }

        public void heartbeat(long timeout) {
            long sendTime = System.currentTimeMillis();
            RunResult request = RunResult.newBuilder()
                    .setRequest(STREAM_HEARTBEAT)
                    .build();

            try {
                this.heartbeatFuture = new CompletableFuture<>();
                this.clientCall.sendMessage(request);
                long recvTime = this.heartbeatFuture.get(timeout, TimeUnit.MILLISECONDS);
                log.info("流心跳检测: 正常, {}ms", recvTime - sendTime);
            } catch (InterruptedException e) {
                log.warn("流心跳: 任务被中断");
            } catch (TimeoutException e) {
                log.warn("流心跳: 超时");
                this.onClose(Status.UNKNOWN, null);
            } catch (Exception e) {
                log.warn("流心跳: 发送心跳异常", e);
                this.onClose(Status.UNKNOWN, null);
            } finally {
                this.heartbeatFuture = null;
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            if (status.getCode() != Status.Code.CANCELLED) {
                this.closedCallback.handle(status, trailers);
            }
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(RunRequest request) {
            LoggerContexts.initial().setModule(DriverModules.RUN);
            Logger logger = LoggerFactory.getLogger("run-stream");

            String req = request.getRequest();
            String serialNo = request.getSerialNo();

            // 如果是心跳响应
            if (STREAM_HEARTBEAT.equals(req)) {
                if (this.heartbeatFuture != null) {
                    this.heartbeatFuture.complete(System.currentTimeMillis());
                } else {
                    logger.warn("流心跳: 接收到心跳响应, 但 future 为空");
                }
                return;
            }

            logger.info("接收到指令请求, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

            CompletableFuture.supplyAsync(() -> {
                LoggerContext context = LoggerContexts.push();
                context.withTable(request.getTableId()).withDevice(request.getId());

                logger.info("开始执行指令, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

                Result result = new Result();
                result.setCode(200);

                try {
                    Object command = new Gson().fromJson(request.getCommand().toStringUtf8(), this.commandType);
                    Cmd<Object> cmd = new Cmd<>(req, request.getTableId(), request.getId(), serialNo, command);
                    Object runResult = this.driverApp.run(cmd);
                    result.setResult(runResult);
                    logger.info("指令执行成功, req = {}, serialNo = {}, command = {}, result = {}",
                            req, serialNo, request.getCommand().toStringUtf8(), runResult);
                } catch (JsonSyntaxException e) {
                    logger.error("指令执行失败, 解析命令失败, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                    result.setCode(400);
                    result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                } catch (Exception e) {
                    logger.error("指令执行失败, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                    result.setCode(400);
                    result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                }
                return result;
            }, this.executor).handle((r, e) -> {
                try {
                    clientCall.sendMessage(RunResult.newBuilder()
                            .setRequest(req)
                            .setMessage(GrpcDriverEventListener.encode(r))
                            .build());
                } catch (Exception ex) {
                    logger.error("上报指令下发结果失败, req = {}, serialNo = {}, command = {}",
                            req, serialNo, request.getCommand().toStringUtf8(), ex);
                }

                return r;
            }).whenComplete((r, e) -> {
                LoggerContexts.pop();
            });
        }
    }

    static class WriteTagHandler extends ClientCall.Listener<RunRequest> {
        private final Logger log = LoggerFactory.withContext().module(DriverModules.WRITE_TAG).getStaticLogger("write-tag-stream");

        private final ThreadPoolExecutor executor;
        private final ClientCall<RunResult, RunRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final Type commandType;
        private final StreamClosedCallback closedCallback;

        // 心跳
        private CompletableFuture<Long> heartbeatFuture;

        public WriteTagHandler(ThreadPoolExecutor executor, ClientCall<RunResult, RunRequest> clientCall,
                               DriverApp<Object, Object, Object> driverApp,
                               Type commandType, StreamClosedCallback closedCallback) {
            this.executor = executor;
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.commandType = commandType;
            this.closedCallback = closedCallback;
        }

        public void heartbeat(long timeout) {
            long sendTime = System.currentTimeMillis();
            RunResult request = RunResult.newBuilder()
                    .setRequest(STREAM_HEARTBEAT)
                    .build();

            try {
                this.heartbeatFuture = new CompletableFuture<>();
                this.clientCall.sendMessage(request);
                long recvTime = this.heartbeatFuture.get(timeout, TimeUnit.MILLISECONDS);
                log.info("流心跳检测: 正常, {}ms", recvTime - sendTime);
            } catch (InterruptedException e) {
                log.warn("流心跳: 任务被中断");
            } catch (TimeoutException e) {
                log.warn("流心跳: 超时");
                this.onClose(Status.UNKNOWN, null);
            } catch (Exception e) {
                log.warn("流心跳: 发送心跳异常", e);
                this.onClose(Status.UNKNOWN, null);
            } finally {
                this.heartbeatFuture = null;
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            if (status.getCode() != Status.Code.CANCELLED) {
                this.closedCallback.handle(status, trailers);
            }
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(RunRequest request) {
            LoggerContexts.initial().setModule(DriverModules.WRITE_TAG);
            Logger logger = LoggerFactory.getLogger("write-tag-stream");

            String req = request.getRequest();
            String serialNo = request.getSerialNo();

            // 如果是心跳响应
            if (STREAM_HEARTBEAT.equals(req)) {
                if (this.heartbeatFuture != null) {
                    this.heartbeatFuture.complete(System.currentTimeMillis());
                } else {
                    logger.warn("流心跳: 接收到心跳响应, 但 future 为空");
                }
                return;
            }

            logger.info("接收到写数据点指令请求, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

            CompletableFuture.supplyAsync(() -> {
                LoggerContext context = LoggerContexts.push();
                context.withTable(request.getTableId()).withDevice(request.getId());

                logger.info("执行写数据点指令, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

                Result result = new Result();
                result.setCode(200);

                try {
                    Object command = new Gson().fromJson(request.getCommand().toStringUtf8(), this.commandType);
                    Cmd<Object> cmd = new Cmd<>(req, request.getTableId(), request.getId(), serialNo, command);
                    Object runResult = this.driverApp.writeTag(cmd);
                    result.setResult(runResult);
                    logger.info("写数据点成功, req = {}, serialNo = {}, command = {}, result = {}",
                            req, serialNo, request.getCommand().toStringUtf8(), runResult);
                } catch (JsonSyntaxException e) {
                    logger.error("写数据点失败, 解析命令失败, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                    result.setCode(400);
                    result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                } catch (Exception e) {
                    logger.error("写数据点失败, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                    result.setCode(400);
                    result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                }
                return result;
            }, this.executor).handle((r, e) -> {
                try {
                    clientCall.sendMessage(RunResult.newBuilder()
                            .setRequest(req)
                            .setMessage(GrpcDriverEventListener.encode(r))
                            .build());
                } catch (Exception ex) {
                    logger.error("上报写数据点结果失败, req = {}, serialNo = {}, command = {}",
                            req, serialNo, request.getCommand().toStringUtf8(), ex);
                }
                return r;
            }).whenComplete((r, e) -> {
                LoggerContexts.pop();
            });
        }
    }

    static class BatchRunHandler extends ClientCall.Listener<BatchRunRequest> {
        private final Logger log = LoggerFactory.withContext().module(DriverModules.BATCH_RUN).getStaticLogger("batch-run-stream");

        private final ThreadPoolExecutor executor;
        private final ClientCall<BatchRunResult, BatchRunRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final Type commandType;
        private final StreamClosedCallback closedCallback;

        // 心跳
        private CompletableFuture<Long> heartbeatFuture;

        public BatchRunHandler(ThreadPoolExecutor executor, ClientCall<BatchRunResult, BatchRunRequest> clientCall,
                               DriverApp<Object, Object, Object> driverApp,
                               Type commandType, StreamClosedCallback closedCallback) {
            this.executor = executor;
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.commandType = commandType;
            this.closedCallback = closedCallback;
        }

        public void heartbeat(long timeout) {
            long sendTime = System.currentTimeMillis();
            BatchRunResult request = BatchRunResult.newBuilder()
                    .setRequest(STREAM_HEARTBEAT)
                    .build();

            try {
                this.heartbeatFuture = new CompletableFuture<>();
                this.clientCall.sendMessage(request);
                long recvTime = this.heartbeatFuture.get(timeout, TimeUnit.MILLISECONDS);
                log.info("流心跳检测: 正常, {}ms", recvTime - sendTime);
            } catch (InterruptedException e) {
                log.warn("流心跳: 任务被中断");
            } catch (TimeoutException e) {
                log.warn("流心跳: 超时");
                this.onClose(Status.UNKNOWN, null);
            } catch (Exception e) {
                log.warn("流心跳: 发送心跳异常", e);
                this.onClose(Status.UNKNOWN, null);
            } finally {
                this.heartbeatFuture = null;
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            if (status.getCode() != Status.Code.CANCELLED) {
                this.closedCallback.handle(status, trailers);
            }
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(BatchRunRequest request) {
            LoggerContexts.initial().setModule(DriverModules.BATCH_RUN);
            Logger logger = LoggerFactory.getLogger("batch-run-stream");

            String req = request.getRequest();
            String serialNo = request.getSerialNo();

            // 如果是心跳响应
            if (STREAM_HEARTBEAT.equals(req)) {
                if (this.heartbeatFuture != null) {
                    this.heartbeatFuture.complete(System.currentTimeMillis());
                } else {
                    logger.warn("流心跳: 接收到心跳响应, 但 future 为空");
                }
                return;
            }

            logger.info("接收到批量执行指令请求, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

            CompletableFuture.supplyAsync(() -> {
                LoggerContext context = LoggerContexts.push();
                context.withTable(request.getTableId());

                logger.info("开始批量执行指令, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8());

                Result result = new Result();
                result.setCode(200);

                try {
                    Object command = new Gson().fromJson(request.getCommand().toStringUtf8(), this.commandType);
                    BatchCmd<Object> cmd = new BatchCmd<>(req, request.getTableId(), request.getIdList(), serialNo, command);
                    Object runResult = this.driverApp.batchRun(cmd);
                    result.setResult(runResult);
                    logger.info("批量下发指令, 成功, req = {}, serialNo = {}, command = {}, result = {}",
                            req, serialNo, request.getCommand().toStringUtf8(), runResult);
                } catch (JsonSyntaxException e) {
                    logger.error("批量下发指令, 解析命令失败, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                    result.setCode(400);
                    result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                } catch (Exception e) {
                    logger.error("批量下发指令, req = {}, serialNo = {}, command = {}", req, serialNo, request.getCommand().toStringUtf8(), e);
                    result.setCode(400);
                    result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                }
                return result;
            }, this.executor).handle((r, e) -> {
                try {
                    clientCall.sendMessage(BatchRunResult.newBuilder()
                            .setRequest(req)
                            .setMessage(GrpcDriverEventListener.encode(r))
                            .build());
                } catch (Exception ex) {
                    logger.error("上报批量下发指令结果失败, req = {}, serialNo = {}, command = {}",
                            req, serialNo, request.getCommand().toStringUtf8(), ex);
                }

                return r;
            }).whenComplete((r, e) -> {
                LoggerContexts.pop();
            });
        }
    }

    static class DebugHandler extends ClientCall.Listener<Debug> {
        private final Logger log = LoggerFactory.withContext().module(DriverModules.DEBUG).getStaticLogger("debug-stream");
        private final ClientCall<Debug, Debug> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final StreamClosedCallback closedCallback;

        // 心跳
        private CompletableFuture<Long> heartbeatFuture;

        public DebugHandler(ClientCall<Debug, Debug> clientCall,
                            DriverApp<Object, Object, Object> driverApp,
                            StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.closedCallback = closedCallback;
        }

        public void heartbeat(long timeout) {
            long sendTime = System.currentTimeMillis();
            Debug request = Debug.newBuilder()
                    .setRequest(STREAM_HEARTBEAT)
                    .build();

            try {
                this.heartbeatFuture = new CompletableFuture<>();
                this.clientCall.sendMessage(request);
                long recvTime = this.heartbeatFuture.get(timeout, TimeUnit.MILLISECONDS);
                log.info("流心跳检测: 正常, {}ms", recvTime - sendTime);
            } catch (InterruptedException e) {
                log.warn("流心跳: 任务被中断");
            } catch (TimeoutException e) {
                log.warn("流心跳: 超时");
                this.onClose(Status.UNKNOWN, null);
            } catch (Exception e) {
                log.warn("流心跳: 发送心跳异常", e);
                this.onClose(Status.UNKNOWN, null);
            } finally {
                this.heartbeatFuture = null;
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            log.error("closed, status = {}, metadata = {}", status, trailers);
            if (status.getCode() != Status.Code.CANCELLED) {
                this.closedCallback.handle(status, trailers);
            }
        }

        @Override
        public void onReady() {
            log.info("ready");
        }

        @Override
        public void onMessage(Debug request) {
            LoggerContexts.initial().setModule(DriverModules.DEBUG);
            Logger logger = LoggerFactory.getLogger("debug-stream");

            String req = request.getRequest();

            // 如果是心跳响应
            if (STREAM_HEARTBEAT.equals(req)) {
                if (this.heartbeatFuture != null) {
                    this.heartbeatFuture.complete(System.currentTimeMillis());
                } else {
                    logger.warn("流心跳: 接收到心跳响应, 但 future 为空");
                }
                return;
            }

            logger.info("debug, req = {}", req);
            Debug debug;
            try {
                debug = this.driverApp.debug(request);
                logger.debug("debug, req = {}, result = {}", req, debug);

                if (debug == null) {
                    logger.warn("debug, req = {}, 无返回值", req);
                    return;
                }

                debug = debug.toBuilder().setRequest(req).build();
            } catch (Exception e) {
                log.error("debug, req = {}", req, e);
                return;
            } finally {
                LoggerContexts.destroy();
            }

            try {
                clientCall.sendMessage(debug);
            } catch (Exception e) {
                log.error("上报 debug 结果失败, req = {}, result = {}", req, debug, e);
            }
        }
    }

    static class StartHandler extends ClientCall.Listener<StartRequest> {
        private final Logger logger = LoggerFactory.withContext().module(DriverModules.START).getStaticLogger("start-stream");
        private final Map<String, Level> loggerRoots;
        private final ClientCall<StartResult, StartRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final GlobalContext globalContext;
        private final Type driverConfigType;
        private final Type tagType;
        private final StreamClosedCallback closedCallback;
        private final Consumer<DriverSingleConfig<BasicConfig<?>>> clearCacheFn;

        // 心跳
        private CompletableFuture<Long> heartbeatFuture;

        public StartHandler(ClientCall<StartResult, StartRequest> clientCall,
                            DriverApp<Object, Object, Object> driverApp,
                            GlobalContext globalContext,
                            Type driverConfigType, Type tagType,
                            StreamClosedCallback closedCallback,
                            Map<String, Level> loggerRoots,
                            Consumer<DriverSingleConfig<BasicConfig<?>>> clearCacheFn
        ) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.globalContext = globalContext;
            this.driverConfigType = driverConfigType;
            this.tagType = tagType;
            this.closedCallback = closedCallback;
            this.loggerRoots = loggerRoots;
            this.clearCacheFn = clearCacheFn;
        }

        public void heartbeat(long timeout) {
            long sendTime = System.currentTimeMillis();
            StartResult request = StartResult.newBuilder()
                    .setRequest(STREAM_HEARTBEAT)
                    .build();

            try {
                this.heartbeatFuture = new CompletableFuture<>();
                this.clientCall.sendMessage(request);
                long recvTime = this.heartbeatFuture.get(timeout, TimeUnit.MILLISECONDS);
                logger.info("流心跳检测: 正常, {}ms", recvTime - sendTime);
            } catch (InterruptedException e) {
                logger.warn("流心跳: 任务被中断");
            } catch (TimeoutException e) {
                logger.warn("流心跳: 超时");
                this.onClose(Status.UNKNOWN, null);
            } catch (Exception e) {
                logger.warn("流心跳: 发送心跳异常", e);
                this.onClose(Status.UNKNOWN, null);
            } finally {
                this.heartbeatFuture = null;
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            logger.error("closed, status = {}, metadata = {}", status, trailers);
            if (status.getCode() != Status.Code.CANCELLED) {
                this.closedCallback.handle(status, trailers);
            }
        }

        @Override
        public void onReady() {
            logger.info("ready");
        }

        @Override
        public void onMessage(StartRequest message) {
            String req = message.getRequest();

            // 如果是心跳响应
            if (STREAM_HEARTBEAT.equals(req)) {
                if (this.heartbeatFuture != null) {
                    this.heartbeatFuture.complete(System.currentTimeMillis());
                } else {
                    logger.warn("流心跳: 接收到心跳响应, 但 future 为空");
                }
                return;
            }

            String config = message.getConfig().toString(StandardCharsets.UTF_8);

            LoggerContexts.initial().setModule(DriverModules.START);
            Logger logger = LoggerFactory.getLogger(StartHandler.class);

            logger.info("启动驱动, req = {}", req);
            if (logger.isDebugEnabled()) {
                logger.debug("启动驱动, req = {}, config  {}", req, config);
            }

            Result result = new Result();
            result.setCode(200);
            result.setResult("启动成功");

            boolean passed = true;
            DriverSingleConfig<BasicConfig<? extends Tag>> driverConfig = null;
            try {
                Type baseConfigType = TypeReference.parametricType(BasicConfig.class, this.tagType);
                Type driverConfigType = TypeReference.parametricType(DriverSingleConfig.class, baseConfigType);

                driverConfig = JSON.parseObject(config, driverConfigType);

                if (logger.isDebugEnabled()) {
                    logger.debug("启动驱动, config = {}", driverConfig);
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
                logger.error("启动驱动, 解析启动配置失败, config = {}", config, e);
                passed = false;
                result.setCode(400);
                result.setResult("启动配置不正确: " + e.getMessage());
                result.setError("启动配置不正确:" + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
            }

            if (driverConfig != null) {
                // 设置驱动组ID
                LoggerContexts.getRootContext().withDriverGroup(driverConfig.getGroupId());

                // 根据驱动实例中的 debug 配置设置 logger 的日志等级
                ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();


                List<String> driverLoggerRoots = driverApp.getDebugLoggerPackages();

                // 如果没有指定日志根节点, 则设置根节点的日志等级
                if (this.loggerRoots.isEmpty() && CollectionUtils.isEmpty(driverLoggerRoots)) {
                    if (driverConfig.isDebug()) {
                        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
                    } else {
                        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);
                    }
                } else {
                    if (driverLoggerRoots != null) {
                        Level level = driverConfig.isDebug() ? Level.DEBUG : Level.INFO;
                        for (String driverLoggerRoot : driverLoggerRoots) {
                            loggerContext.getLogger(driverLoggerRoot).setLevel(level);
                        }
                    }

                    if (driverConfig.isDebug()) {
                        // 如果是调试模式则修改为 DEBUG 等级
                        for (Map.Entry<String, Level> entry : this.loggerRoots.entrySet()) {
                            loggerContext.getLogger(entry.getKey()).setLevel(Level.DEBUG);
                        }
                    } else {
                        // 如果不是调试模式, 则恢复为之前的日志等级
                        for (Map.Entry<String, Level> entry : this.loggerRoots.entrySet()) {
                            loggerContext.getLogger(entry.getKey()).setLevel(entry.getValue());
                        }
                    }
                }
            }

            if (passed) {
                try {
                    Object drvConfig = JSON.parseObject(config, this.driverConfigType);
                    this.driverApp.start(drvConfig);
                    this.clearCacheFn.accept(driverConfig);
                } catch (Exception e) {
                    logger.error("启动驱动:", e);
                    result.setCode(400);
                    result.setResult("启动失败: " + e.getMessage());
                    result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                }
            }

            LoggerContexts.destroy();

            clientCall.sendMessage(StartResult.newBuilder()
                    .setRequest(message.getRequest())
                    .setMessage(GrpcDriverEventListener.encode(result))
                    .build());
        }
    }

    static class SchemaHandler extends ClientCall.Listener<SchemaRequest> {

        private final Logger logger = LoggerFactory.withContext().module(DriverModules.SCHEMA).getStaticLogger("schema-stream");

        private final ClientCall<SchemaResult, SchemaRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final StreamClosedCallback closedCallback;

        // 心跳
        private CompletableFuture<Long> heartbeatFuture;

        public SchemaHandler(ClientCall<SchemaResult, SchemaRequest> clientCall,
                             DriverApp<Object, Object, Object> driverApp,
                             StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.closedCallback = closedCallback;
        }

        public void heartbeat(long timeout) {
            long sendTime = System.currentTimeMillis();
            SchemaResult request = SchemaResult.newBuilder()
                    .setRequest(STREAM_HEARTBEAT)
                    .build();

            try {
                this.heartbeatFuture = new CompletableFuture<>();
                this.clientCall.sendMessage(request);
                long recvTime = this.heartbeatFuture.get(timeout, TimeUnit.MILLISECONDS);
                logger.info("流心跳检测: 正常, {}ms", recvTime - sendTime);
            } catch (InterruptedException e) {
                logger.warn("流心跳: 任务被中断");
            } catch (TimeoutException e) {
                logger.warn("流心跳: 超时");
                this.onClose(Status.UNKNOWN, null);
            } catch (Exception e) {
                logger.warn("流心跳: 发送心跳异常", e);
                this.onClose(Status.UNKNOWN, null);
            } finally {
                this.heartbeatFuture = null;
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            logger.error("closed, status = {}, metadata = {}", status, trailers);
            if (status.getCode() != Status.Code.CANCELLED) {
                this.closedCallback.handle(status, trailers);
            }
        }

        @Override
        public void onReady() {
            logger.info("ready");
        }

        @Override
        public void onMessage(SchemaRequest request) {
            String req = request.getRequest();

            // 如果是心跳响应
            if (STREAM_HEARTBEAT.equals(req)) {
                if (this.heartbeatFuture != null) {
                    this.heartbeatFuture.complete(System.currentTimeMillis());
                } else {
                    logger.warn("流心跳: 接收到心跳响应, 但 future 为空");
                }
                return;
            }

            logger.info("req = {}, type = schema", request.getRequest());

            Result result = new Result();
            try {
                String schema = this.driverApp.schema();
                if (logger.isDebugEnabled()) {
                    logger.debug("req = {}, type = schema, {}", request.getRequest(), schema);
                }

                if (schema != null) {
                    // 替换版本号
                    schema = schema.replaceAll("__version__", driverApp.getVersion());
                    schema = schema.replaceAll("__sdk_version__", GlobalContext.getVersion());
                }

                result.setCode(200);
                result.setResult(schema);
            } catch (Exception e) {
                logger.error("req = {}, type = schema", request.getRequest(), e);
                result.setCode(400);
                result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            }

            String message = new Gson().toJson(result);
            clientCall.sendMessage(SchemaResult.newBuilder()
                    .setRequest(request.getRequest())
                    .setMessage(ByteString.copyFrom(message, StandardCharsets.UTF_8))
                    .build());
        }
    }

    static class HttpProxyHandler extends ClientCall.Listener<HttpProxyRequest> {
        private final Logger logger = LoggerFactory.withContext().module(DriverModules.HTTP_PROXY).getStaticLogger("http-proxy-stream");

        private final static Gson GSON = new Gson();

        private final static Type HEADER_TYPE = new TypeToken<Map<String, List<String>>>() {
        }.getType();

        private final ClientCall<HttpProxyResult, HttpProxyRequest> clientCall;
        private final DriverApp<Object, Object, Object> driverApp;
        private final StreamClosedCallback closedCallback;

        // 心跳
        private CompletableFuture<Long> heartbeatFuture;

        public HttpProxyHandler(ClientCall<HttpProxyResult, HttpProxyRequest> clientCall,
                                DriverApp<Object, Object, Object> driverApp,
                                StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.driverApp = driverApp;
            this.closedCallback = closedCallback;
        }

        public void heartbeat(long timeout) {
            long sendTime = System.currentTimeMillis();
            HttpProxyResult request = HttpProxyResult.newBuilder()
                    .setRequest(STREAM_HEARTBEAT)
                    .build();

            try {
                this.heartbeatFuture = new CompletableFuture<>();
                this.clientCall.sendMessage(request);
                long recvTime = this.heartbeatFuture.get(timeout, TimeUnit.MILLISECONDS);
                logger.info("流心跳检测: 正常, {}ms", recvTime - sendTime);
            } catch (InterruptedException e) {
                logger.warn("流心跳: 任务被中断");
            } catch (TimeoutException e) {
                logger.warn("流心跳: 超时");
                this.onClose(Status.UNKNOWN, null);
            } catch (Exception e) {
                logger.warn("流心跳: 发送心跳异常", e);
                this.onClose(Status.UNKNOWN, null);
            } finally {
                this.heartbeatFuture = null;
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            logger.error("closed, status = {}, metadata = {}", status, trailers);
            if (status.getCode() != Status.Code.CANCELLED) {
                this.closedCallback.handle(status, trailers);
            }
        }

        @Override
        public void onReady() {
            logger.info("ready");
        }

        @Override
        public void onMessage(HttpProxyRequest request) {
            String req = request.getRequest();

            // 如果是心跳响应
            if (STREAM_HEARTBEAT.equals(req)) {
                if (this.heartbeatFuture != null) {
                    this.heartbeatFuture.complete(System.currentTimeMillis());
                } else {
                    logger.warn("流心跳: 接收到心跳响应, 但 future 为空");
                }
                return;
            }

            logger.info("req = {}, type = httpProxy", req);
            Result result = new Result();
            try {
                Map<String, List<String>> headers = GSON.fromJson(request.getHeaders().toStringUtf8(), HEADER_TYPE);
                Object proxyResult = this.driverApp.httpProxy(request.getType(), headers, request.getData().toByteArray());
                if (logger.isDebugEnabled()) {
                    logger.debug("req = {}, type = httpProxy, {}", request.getRequest(), proxyResult);
                }

                result.setCode(200);
                result.setResult(proxyResult);
            } catch (Exception e) {
                logger.error("req = {}, type = schema", request.getRequest(), e);
                result.setCode(400);
                result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            }

            String message = GSON.toJson(result);
            clientCall.sendMessage(HttpProxyResult.newBuilder()
                    .setRequest(request.getRequest())
                    .setData(ByteString.copyFrom(message, StandardCharsets.UTF_8))
                    .build());
        }
    }

    @FunctionalInterface
    interface StreamClosedCallback {
        void handle(Status status, Metadata trailers);
    }

    static class OnceStreamClosedCallback implements StreamClosedCallback {
        private final AtomicBoolean called = new AtomicBoolean(false);
        private final StreamClosedCallback delegate;

        public OnceStreamClosedCallback(StreamClosedCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handle(Status status, Metadata trailers) {
            if (this.called.compareAndSet(false, true)) {
                this.delegate.handle(status, trailers);
            }
        }
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
