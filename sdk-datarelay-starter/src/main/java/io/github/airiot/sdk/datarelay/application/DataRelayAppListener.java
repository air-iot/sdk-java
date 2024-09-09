package io.github.airiot.sdk.datarelay.application;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.datarelay.DataRelayModules;
import io.github.airiot.sdk.datarelay.configuration.properties.DataRelayAppProperties;
import io.github.airiot.sdk.datarelay.configuration.properties.DataRelayGrpcProperties;
import io.github.airiot.sdk.datarelay.grpc.Error;
import io.github.airiot.sdk.datarelay.grpc.*;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DataRelayAppListener implements SmartLifecycle {

    private final Logger logger = LoggerFactory.withContext().module(DataRelayModules.CONNECT_PLATFORM).getStaticLogger(DataRelayAppListener.class);
    private final Logger healthCheckLogger = LoggerFactory.withContext().module(DataRelayModules.HEARTBEAT).getStaticLogger(DataRelayAppListener.class);

    private final DataRelayAppProperties appProperties;
    private final DataRelayGrpcProperties grpcProperties;
    private final DataRelayAppProxy dataRelayApp;
    private final DataRelayInstanceServiceGrpc.DataRelayInstanceServiceBlockingStub dataRelayClient;
    private final Metadata metadata;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);

    private ClientCall<Result, DataRelayInstanceStartRequest> startCall = null;
    private ClientCall<Result, HttpProxyRequest> httpProxyCall = null;
    private Thread connectThread;
    private Thread healthCheckThread;
    private long lastConnectTime;

    public DataRelayAppListener(DataRelayAppProperties appProperties,
                                DataRelayGrpcProperties grpcProperties,
                                DataRelayApp<Object> app,
                                DataRelayInstanceServiceGrpc.DataRelayInstanceServiceBlockingStub dataRelayClient) {
        this.appProperties = appProperties;
        this.grpcProperties = grpcProperties;
        this.dataRelayApp = new DataRelayAppProxy(app);

        this.metadata = new Metadata();
        metadata.put(Metadata.Key.of("projectId", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(appProperties.getProjectId().getBytes(StandardCharsets.UTF_8)));
        metadata.put(Metadata.Key.of("instanceId", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(appProperties.getInstanceId().getBytes(StandardCharsets.UTF_8)));
        metadata.put(Metadata.Key.of("id", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(appProperties.getId().getBytes(StandardCharsets.UTF_8)));
        metadata.put(Metadata.Key.of("name", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(appProperties.getName().getBytes(StandardCharsets.UTF_8)));

        ClientInterceptor metadataInterceptor = MetadataUtils.newAttachHeadersInterceptor(new Metadata());
        this.dataRelayClient = dataRelayClient.withInterceptors(metadataInterceptor);
    }

    @Override
    public void start() {
        if (!state.compareAndSet(State.CLOSED, State.CONNECTING)) {
            logger.warn("服务已启动, 无需重复启动");
            return;
        }

        this.connect();
    }

    @Override
    public void stop() {
        if (!state.get().isRunning()) {
            logger.info("服务已停止");
            return;
        }

        logger.info("服务停止中");

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
            this.dataRelayApp.stop();
        } catch (Exception e) {
            logger.warn("停止服务异常", e);
        }

        state.set(State.CLOSED);
        logger.info("服务已停止");
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
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
//        this.healthCheckThread.setDaemon(true);
        this.healthCheckThread.start();
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
                HealthCheckResponse response = this.dataRelayClient.healthCheck(HealthCheckRequest.newBuilder()
                        .setProjectId(this.appProperties.getProjectId())
                        .setService(this.appProperties.getInstanceId())
                        .setType(this.appProperties.getId())
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
            healthCheckLogger.info("重新连接DataRelay服务");
            this.state.set(State.RECONNECTING);
            this.connect();
        }
    }

    /**
     * 连接DataRelay服务
     */
    private void connect() {
        if (this.connectThread != null) {
            this.connectThread.interrupt();
            this.connectThread = null;
        }

        if (this.startCall != null) {
            this.startCall.cancel("重新连接", null);
        }
        if (this.httpProxyCall != null) {
            this.httpProxyCall.cancel("重新连接", null);
        }

        logger.info("创建连接 DataRelay 服务线程");
        this.connectThread = new Thread(this::connectTask);
        this.connectThread.setName("connectTask");
        this.connectThread.start();
    }

    /**
     * 连接平台
     */
    private void connectTask() {
        Channel channel = this.dataRelayClient.getChannel();
        int retryTimes = 0;
        long retryInterval = this.grpcProperties.getReconnectInterval().toMillis();

        if (this.lastConnectTime != 0) {
            long waitTime = retryInterval - (System.currentTimeMillis() - this.lastConnectTime);
            if (waitTime > 0) {
                try {
                    logger.info("连接DataRelay服务: 等待 {}ms", waitTime);
                    TimeUnit.MILLISECONDS.sleep(waitTime);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        this.lastConnectTime = System.currentTimeMillis();

        logger.info("连接DataRelay服务线程已启动, 重连间隔 {}ms", retryInterval);

        while (this.state.get().isRunning()) {
            retryTimes++;

            logger.info("连接DataRelay服务: 第 {} 次连接", retryTimes);

            StreamClosedCallback callback = new OnceStreamClosedCallback(this::handleStreamClosed);

            try {
                // start
                this.startCall = channel.newCall(
                        DataRelayInstanceServiceGrpc.getStartStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata startMetadata = new Metadata();
                startMetadata.merge(this.metadata);
                StartHandler startHandler = new StartHandler(this.startCall, this.dataRelayApp, callback);
                this.startCall.start(startHandler, startMetadata);
                this.startCall.request(Integer.MAX_VALUE);

                // httpProxy
                this.httpProxyCall = channel.newCall(
                        DataRelayInstanceServiceGrpc.getHttpProxyStreamMethod(),
                        CallOptions.DEFAULT.withWaitForReady()
                );
                Metadata httpProxyMetadata = new Metadata();
                httpProxyMetadata.merge(this.metadata);
                HttpProxyHandler httpProxyHandler = new HttpProxyHandler(this.httpProxyCall, this.dataRelayApp, callback);
                this.httpProxyCall.start(httpProxyHandler, httpProxyMetadata);
                this.httpProxyCall.request(Integer.MAX_VALUE);

                this.state.set(State.RUNNING);

                logger.info("连接 DataRelay 服务: 第 {} 次连接成功", retryTimes);

                break;
            } catch (Exception e) {
                logger.error("连接 DataRelay 服务: 第 {} 次连接失败", retryTimes, e);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(retryInterval);
            } catch (InterruptedException e) {
                logger.info("连接 DataRelay 服务: 被终止");
                return;
            }
        }

        if (State.RUNNING.equals(this.state.get())) {
            this.startHealthCheck();
        }
    }

    private void handleStreamClosed(Status status, Metadata trailers) {
        logger.warn("stream closed, reconnecting, status = {}, metadata = {}", status, trailers);
        if (State.RUNNING.equals(this.state.get())) {
            this.state.set(State.RECONNECTING);
            this.connect();
        }
    }

    static class StartHandler extends ClientCall.Listener<DataRelayInstanceStartRequest> {
        private final Logger logger = LoggerFactory.withContext().module(DataRelayModules.START).getStaticLogger(StartHandler.class);
        private final ClientCall<Result, DataRelayInstanceStartRequest> clientCall;
        private final DataRelayAppProxy proxy;
        private final StreamClosedCallback closedCallback;

        public StartHandler(ClientCall<Result, DataRelayInstanceStartRequest> clientCall,
                            DataRelayAppProxy proxy,
                            StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.proxy = proxy;
            this.closedCallback = closedCallback;
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
        public void onMessage(DataRelayInstanceStartRequest message) {
            String req = message.getRequest();
            String config = message.getData().toString(StandardCharsets.UTF_8);

            LoggerContexts.initial().setModule(DataRelayModules.START);
            Logger logger = LoggerFactory.getLogger(StartHandler.class);

            logger.info("启动服务, req = {}", req);
            if (logger.isDebugEnabled()) {
                logger.debug("启动服务, req = {}, config  {}", req, config);
            }

            Result.Builder builder = Result.newBuilder();
            builder.setRequest(req);

            try {
                this.proxy.start(config);
                builder.setStatus(true).setInfo("启动成功");
            } catch (Exception e) {
                logger.error("启动服务:", e);
                String msg = e.getMessage();
                builder.setStatus(false).setInfo("启动失败").setDetail(msg == null ? e.getClass().toString() : msg);
            } finally {
                LoggerContexts.destroy();
            }

            clientCall.sendMessage(builder.build());
        }
    }

    static class HttpProxyHandler extends ClientCall.Listener<HttpProxyRequest> {
        private final Logger logger = LoggerFactory.withContext().module(DataRelayModules.HTTP_PROXY).getStaticLogger(HttpProxyHandler.class);

        private final static Gson GSON = new Gson();

        private final static Type HEADER_TYPE = new TypeToken<Map<String, List<String>>>() {
        }.getType();

        private final ClientCall<Result, HttpProxyRequest> clientCall;
        private final DataRelayAppProxy proxy;
        private final StreamClosedCallback closedCallback;

        public HttpProxyHandler(ClientCall<Result, HttpProxyRequest> clientCall,
                                DataRelayAppProxy proxy,
                                StreamClosedCallback closedCallback) {
            this.clientCall = clientCall;
            this.proxy = proxy;
            this.closedCallback = closedCallback;
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
            LoggerContexts.initial().setModule(DataRelayModules.HTTP_PROXY);
            Logger logger = LoggerFactory.getLogger(HttpProxyHandler.class);

            logger.info("req = {}, type = httpProxy", request.getRequest());
            Result.Builder builder = Result.newBuilder();
            builder.setRequest(request.getRequest());

            try {
                Map<String, List<String>> headers = GSON.fromJson(request.getHeaders().toStringUtf8(), HEADER_TYPE);
                Object proxyResult = this.proxy.proxy(request.getType(), headers, request.getData().toStringUtf8());
                if (logger.isDebugEnabled()) {
                    logger.debug("req = {}, type = httpProxy, {}", request.getRequest(), proxyResult);
                }

                String result = "";
                if (proxyResult != null) {
                    if (proxyResult instanceof String) {
                        result = (String) proxyResult;
                    } else {
                        result = GSON.toJson(proxyResult);
                    }
                }

                builder.setStatus(true)
                        .setInfo("请求成功")
                        .setResult(ByteString.copyFrom(result, StandardCharsets.UTF_8));
            } catch (Exception e) {
                logger.error("req = {}, type = httpProxy", request.getRequest(), e);
                String msg = e.getMessage();
                builder.setStatus(false)
                        .setInfo("请求失败")
                        .setDetail(msg == null ? e.getClass().toString() : msg);
            } finally {
                LoggerContexts.destroy();
            }
            
            clientCall.sendMessage(builder.build());
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
