package io.github.airiot.sdk.driver.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.github.airiot.sdk.driver.*;
import io.github.airiot.sdk.driver.config.BasicConfig;
import io.github.airiot.sdk.driver.config.DriverSingleConfig;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.data.DataDispatcher;
import io.github.airiot.sdk.driver.data.DataDispatchers;
import io.github.airiot.sdk.driver.data.PointSerializationAdapter;
import io.github.airiot.sdk.driver.listener.BatchCmd;
import io.github.airiot.sdk.driver.listener.Cmd;
import io.github.airiot.sdk.driver.listener.Result;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.driver.model.Tag;
import io.github.airiot.sdk.logger.LoggerContext;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;


/**
 * AI 扩展服务接口
 */
public class AIServer implements SmartLifecycle, DataDispatcher {

    private final static String INDEX_HTML_FILE = "index.html";

    private final Logger logger = LoggerFactory.withContext().getDynamicLogger(AIServer.class);

    private final Gson pointGson = new GsonBuilder()
            .registerTypeAdapterFactory(PointSerializationAdapter.newFactory())
            .create();

    private final AtomicBoolean started = new AtomicBoolean();
    private final Gson gson = new Gson();
    private final DriverAppProperties properties;
    private final GlobalContext globalContext;
    private final DriverApp<Object, Object, Object> driverApp;
    private final DataDispatchers dispatchers;

    private final Path configFileDir;
    private final Path configFile;
    private final String configFileName;

    private final Type configType;
    private final Type cmdType;
    private final Type tagType;
    private final AtomicReference<SubscriptionMatcher> realtimeSubscriptionMatcher = new AtomicReference<>(null);
    private final AtomicReference<SubscriptionMatcher> deviceStatusSubscriptionMatcher = new AtomicReference<>(null);
    private final Sinks.Many<Point> realtimeSinks = Sinks.many().multicast().directBestEffort();
    private final Sinks.Many<WebsocketSubscription.DeviceStatusMessagePayload> deviceStatusSinks = Sinks.many().multicast().directBestEffort();

    /**
     * 设备状态缓存。
     * <br>
     * key: 表标识#设备编号. 例如: opcda#opcda001
     * value: 设备状态数据
     */
    private final Map<String, WebsocketSubscription.DeviceStatusMessagePayload> deviceStatus = new ConcurrentHashMap<>();

    private DisposableServer httpServer;

    private Thread httpServerThread;
    private Thread deviceStatusThread;
    private Thread watchThread;

    public AIServer(DriverAppProperties properties,
                    GlobalContext globalContext,
                    DriverApp<Object, Object, Object> driverApp,
                    DataDispatchers dispatchers) {
        this.properties = properties;
        this.globalContext = globalContext;
        this.driverApp = driverApp;
        this.dispatchers = dispatchers;
        Type[] parameterizedTypes = DriverUtils.parseDriverAppGenericTypes(driverApp);
        this.configType = parameterizedTypes[0];
        this.cmdType = parameterizedTypes[1];
        this.tagType = parameterizedTypes[2];

        this.configFile = Paths.get(properties.getDataFilePath()).normalize().toAbsolutePath();
        this.configFileDir = this.configFile.getParent();
        this.configFileName = this.configFile.toFile().getName();
    }

    void watchConfigFile() {
        logger.info("监听配置文件变化: 监听目录 {} 下文件 {}", this.configFileDir, this.configFileName);
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            this.configFileDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            long lastTriggerTime = System.currentTimeMillis();
            while (true) {
                try {
                    WatchKey key = watcher.take();
                    if (key == null) {
                        continue;
                    }

                    logger.debug("监听配置文件变化: 接收到事件, {}", key);

                    for (WatchEvent<?> pollEvent : key.pollEvents()) {
                        String targetFile = String.valueOf(pollEvent.context());
                        WatchEvent.Kind<?> kind = pollEvent.kind();
                        logger.debug("监听配置文件变化: 接收到事件, kind={}, file={}", kind, targetFile);

                        if (!configFileName.equalsIgnoreCase(targetFile)) {
                            continue;
                        }

                        if (StandardWatchEventKinds.ENTRY_CREATE != kind && StandardWatchEventKinds.ENTRY_MODIFY != kind) {
                            break;
                        }

                        // 如果两次触发间隔少于 1s
                        if (System.currentTimeMillis() - lastTriggerTime < 1000) {
                            break;
                        }

                        logger.info("监听配置文件变化: 监测到配置文件变化, 重载驱动");

                        try {
                            this.startDriver();
                            logger.info("监听配置文件变化: 监测到配置文件变化, 重载驱动完成");
                            lastTriggerTime = System.currentTimeMillis();
                            break;
                        } catch (Exception e) {
                            logger.error("监听配置文件变化: 监测到配置文件发生变化, 但是重载驱动异常", e);
                        }
                    }

                    if (!key.reset()) {
                        logger.warn("监听配置文件变化: 重置监听事件失败");
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.info("监听配置文件变化被终止");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("监听配置文件变化事件异常, {}", this.configFileDir, e);
        }
    }

    @Override
    public void start() {
        if (!this.started.compareAndSet(false, true)) {
            throw new IllegalStateException("AIServer 已启动, 不允许重复启动");
        }

        if (this.properties.isAiServerEnabled()) {
            this.dispatchers.register(this);

            if (this.httpServerThread != null) {
                this.httpServerThread.interrupt();
            }

            this.httpServerThread = new Thread(this::startServer, "AIServer");
            this.httpServerThread.start();
        } else {
            logger.info("AI 功能未启用");
        }

        if (this.properties.isDataFileEnabled()) {
            if (this.watchThread != null) {
                this.watchThread.interrupt();
            }

            this.watchThread = new Thread(this::watchConfigFile, "config-watcher");
            this.watchThread.start();
        } else {
            logger.info("DataFile 功能未启用");
        }

        if (this.deviceStatusThread != null) {
            this.deviceStatusThread.interrupt();
        }

        this.deviceStatusThread = new Thread(this::checkDeviceStatus, "DeviceStatus");
        this.deviceStatusThread.setDaemon(true);
        this.deviceStatusThread.start();

        if (this.configFile.toFile().exists()) {
            try {
                this.startDriver();
            } catch (Exception e) {
                logger.error("启动驱动失败", e);
            }
        }
    }

    @Override
    public void stop() {
        logger.info("关闭 AIServer");

        if (this.watchThread != null) {
            this.watchThread.interrupt();
            this.watchThread = null;
        }

        if (this.httpServer != null) {
            this.httpServer.disposeNow(Duration.ofSeconds(1));
            this.httpServer = null;
        }

        if (this.httpServerThread != null) {
            this.httpServerThread.interrupt();
            this.httpServerThread = null;
        }

        if (this.deviceStatusThread != null) {
            this.deviceStatusThread.interrupt();
            this.deviceStatusThread = null;
        }

        this.started.set(false);
    }

    @Override
    public boolean isRunning() {
        return this.started.get();
    }

    void startServer() {
        String host = StringUtils.hasText(this.properties.getAiServerHost()) ? this.properties.getAiServerHost() : "0.0.0.0";
        logger.info("启动 AIServer, listen on {}:{}", host, this.properties.getAiServerPort());
        Path indexHtml = Paths.get(INDEX_HTML_FILE);
        this.httpServer = HttpServer.create()
                .port(this.properties.getAiServerPort())
                .route(routes -> {
                    routes.file("/", indexHtml);
                    routes.file("/index", indexHtml);
                    routes.file("/index.html", indexHtml);
                    routes.get("/driver/config", this::getConfig);
                    routes.post("/driver/config", this::saveConfig);
                    routes.post("/driver/start", this::handleStart);
                    routes.post("/driver/run", this::handleRun);
                    routes.post("/driver/batch-run", this::handleBatchRun);
                    routes.post("/driver/write-tag", this::handleWriteTag);
                    routes.get("/driver/schema", this::handleSchema);
                    routes.get("/driver/ws", this::handleWebsocket);
                }).bindNow();

        logger.info("AIServer 已启动, listening on {}:{}", host, this.properties.getAiServerPort());

        this.httpServer.onDispose().block();
    }

    void writeConfig(String config) {

    }

    /**
     * 获取当前驱动的配置
     */
    Publisher<Void> getConfig(HttpServerRequest request, HttpServerResponse response) {
        return response.sendFile(this.configFile);
    }

    /**
     * 获取当前驱动的配置
     */
    Publisher<Void> saveConfig(HttpServerRequest request, HttpServerResponse response) {
        return request.receive().aggregate().asString(StandardCharsets.UTF_8).flatMap(config -> {
            try (FileOutputStream fos = new FileOutputStream(this.configFile.toFile())) {
                fos.write(config.getBytes(StandardCharsets.UTF_8));
                return response
                        .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                        .sendString(Mono.just(this.gson.toJson(StatusResult.of("OK")))).then();
            } catch (Exception e) {
                return response.status(400)
                        .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                        .sendString(Mono.just(this.gson.toJson(ErrorResult.of(e.getMessage())))).then();
            }
        });
    }

    /**
     * 从配置文件加载驱动配置
     */
    Object loadConfig() {
        try (Reader reader = new FileReader(this.configFile.toFile())) {
            return this.gson.fromJson(reader, this.configType);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException("读取驱动配置文件失败", e);
        }
    }

    void refreshDevices() {
        try (Reader reader = new FileReader(this.configFile.toFile())) {
            Type configType = TypeToken.getParameterized(BasicConfig.class, this.tagType).getType();
            Type driverConfigType = TypeToken.getParameterized(DriverSingleConfig.class, configType).getType();
            DriverSingleConfig<BasicConfig<? extends Tag>> driverConfig = this.gson.fromJson(reader, driverConfigType);
            if (CollectionUtils.isEmpty(driverConfig.getTables())) {
                return;
            }
            this.globalContext.resetGlobalContext(driverConfig);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            throw new RuntimeException("读取驱动配置文件失败", e);
        }
    }

    /**
     * 从配置文件加载驱动配置并重启驱动
     */
    void startDriver() {
        try {
            this.refreshDevices();
            Object config = this.loadConfig();
            this.driverApp.start(config);
        } catch (Exception e) {
            logger.error("刷新设备配置信息到 GlobalContext 异常", e);
        }
    }

    /**
     * 重启驱动
     */
    Publisher<Void> handleStart(HttpServerRequest request, HttpServerResponse response) {
        return request.receive().aggregate().asString(StandardCharsets.UTF_8).flatMap(config -> {
            try (FileOutputStream fos = new FileOutputStream(this.configFile.toFile())) {
                fos.write(config.getBytes(StandardCharsets.UTF_8));
                this.startDriver();
            } catch (FileNotFoundException ignore) {
            } catch (Exception e) {
                return response.status(400)
                        .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                        .sendString(Mono.just(this.gson.toJson(ErrorResult.of(e.getMessage())))).then();
            }
            return response.header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .sendString(Mono.just(this.gson.toJson(StatusResult.of("OK")))).then();
        });
    }

    /**
     * 下发指令
     */
    Publisher<Void> handleRun(HttpServerRequest request, HttpServerResponse response) {
        LoggerContext loggerContext = LoggerContexts.push();
        loggerContext.setModule(DriverModules.RUN);
        return request.receive().aggregate().asString(StandardCharsets.UTF_8).flatMap(cmdData -> {
            Result result = new Result();
            result.setCode(200);
            try {
                HttpCmd command = this.gson.fromJson(cmdData, HttpCmd.class);
                Map<String, Object> cmdMap = new HashMap<>();
                cmdMap.put("id", command.getName());
                cmdMap.put("name", command.getName());
                cmdMap.put("ops", command.getOps());
                cmdMap.put("params", command.getParams());
                JsonElement jsonElement = this.gson.toJsonTree(cmdMap);
                Object finalCmd = this.gson.fromJson(jsonElement, this.cmdType);
                String reqId = UUID.randomUUID().toString();
                Cmd<Object> targetCmd = new Cmd<>(reqId, command.getTable(), command.getId(), reqId, finalCmd);
                Object runResult = this.driverApp.run(targetCmd);
                result.setResult(runResult);
            } catch (JsonSyntaxException e) {
                logger.error("指令执行失败, 解析命令失败, {}", cmdData, e);
                result.setCode(400);
                result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            } catch (Exception e) {
                logger.error("指令执行失败, {}", cmdData, e);
                result.setCode(400);
                result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            }

            return response
                    .status(result.getCode())
                    .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .sendString(Mono.just(this.gson.toJson(result)))
                    .then();
        });
    }

    /**
     * 批量下发指令
     */
    Publisher<Void> handleBatchRun(HttpServerRequest request, HttpServerResponse response) {
        LoggerContext loggerContext = LoggerContexts.push();
        loggerContext.setModule(DriverModules.RUN);
        return request.receive().aggregate().asString(StandardCharsets.UTF_8).flatMap(cmdData -> {
            Result result = new Result();
            result.setCode(200);
            try {
                HttpBatchCmd command = this.gson.fromJson(cmdData, HttpBatchCmd.class);
                Map<String, Object> cmdMap = new HashMap<>();
                cmdMap.put("id", command.getName());
                cmdMap.put("name", command.getName());
                cmdMap.put("ops", command.getOps());
                cmdMap.put("params", command.getParams());
                JsonElement jsonElement = this.gson.toJsonTree(cmdMap);
                Object finalCmd = this.gson.fromJson(jsonElement, this.cmdType);
                String reqId = UUID.randomUUID().toString();
                BatchCmd<Object> targetCmd = new BatchCmd<>(reqId, command.getTable(), command.getIds(), reqId, finalCmd);
                Object runResult = this.driverApp.batchRun(targetCmd);
                result.setResult(runResult);
            } catch (JsonSyntaxException e) {
                logger.error("指令执行失败, 解析命令失败, {}", cmdData, e);
                result.setCode(400);
                result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            } catch (Exception e) {
                logger.error("指令执行失败, {}", cmdData, e);
                result.setCode(400);
                result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            }

            return response
                    .status(result.getCode())
                    .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .sendString(Mono.just(this.gson.toJson(result))).then();
        });
    }

    /**
     * 写数据点
     */
    Publisher<Void> handleWriteTag(HttpServerRequest request, HttpServerResponse response) {
        LoggerContext loggerContext = LoggerContexts.push();
        loggerContext.setModule(DriverModules.RUN);
        return request.receive().aggregate().asString(StandardCharsets.UTF_8).flatMap(cmdData -> {
            Result result = new Result();
            result.setCode(200);
            try {
                HttpCmd command = this.gson.fromJson(cmdData, HttpCmd.class);
                Map<String, Object> cmdMap = new HashMap<>();
                cmdMap.put("id", command.getName());
                cmdMap.put("name", command.getName());
                cmdMap.put("ops", command.getOps());
                cmdMap.put("params", command.getParams());
                JsonElement jsonElement = this.gson.toJsonTree(cmdMap);
                Object finalCmd = this.gson.fromJson(jsonElement, this.cmdType);
                String reqId = UUID.randomUUID().toString();
                Cmd<Object> targetCmd = new Cmd<>(reqId, command.getTable(), command.getId(), reqId, finalCmd);
                Object runResult = this.driverApp.run(targetCmd);
                result.setResult(runResult);
            } catch (JsonSyntaxException e) {
                logger.error("指令执行失败, 解析命令失败, {}", cmdData, e);
                result.setCode(400);
                result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            } catch (Exception e) {
                logger.error("指令执行失败, {}", cmdData, e);
                result.setCode(400);
                result.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            }

            return response
                    .status(result.getCode())
                    .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .sendString(Mono.just(this.gson.toJson(result))).then();
        });
    }

    /**
     * 获取驱动的 schema 定义
     */
    Publisher<Void> handleSchema(HttpServerRequest request, HttpServerResponse response) {
        HttpHeaders headers = request.requestHeaders();
        String language = headers.get("locale");
        if (!StringUtils.hasLength(language)) {
            language = headers.get("Accept-Language");
        }
        if (!StringUtils.hasLength(language)) {
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> parameters = decoder.parameters();
            List<String> locales = parameters.get("locale");
            language = CollectionUtils.isEmpty(locales) ? "" : locales.getFirst();
        }

        String schema = this.driverApp.schema(language);
        return response.header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .sendString(Mono.just(this.gson.toJson(Collections.singletonMap("schema", schema))));
    }

    Publisher<Void> handleWebsocket(HttpServerRequest request, HttpServerResponse response) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> parameters = decoder.parameters();
        List<String> requestTypes = parameters.get("type");
        String requestType = CollectionUtils.isEmpty(requestTypes) ? "" : requestTypes.getFirst();

        logger.info("WebSocket 连接建立, type={}", requestType);

        // 如果是实时数据
        if (WebsocketSubscription.WEBSOCKET_SUBSCRIPTION_REALTIME.equalsIgnoreCase(requestType)) {
            return response.sendWebsocket((in, out) -> this.handleRealtimeWebsocket(in, out, response));
        } else if (WebsocketSubscription.WEBSOCKET_SUBSCRIPTION_DEVICE_STATUS.equalsIgnoreCase(requestType)) {
            return response.sendWebsocket((in, out) -> this.handleDeviceStateWebsocket(in, out, response));
        }
        return response.status(400).sendString(Mono.just(this.gson.toJson(ErrorResult.of("不支持的类型: " + requestType))));
    }

    /**
     * 处理设备实时数据 websocket 消息推送
     */
    Publisher<Void> handleRealtimeWebsocket(WebsocketInbound inbound, WebsocketOutbound outbound, HttpServerResponse response) {
        // 后续接收到的数据都转换为 RealTimeSubscription 类型
        return inbound.receive()
                .asByteArray()
                .switchMap(bytes -> {
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    WebsocketSubscription.Subscription realTimeSub = this.gson.fromJson(
                            json,
                            WebsocketSubscription.Subscription.class
                    );
                    logger.info("实时数据订阅: tableId={}, deviceId={}",
                            realTimeSub.getTable(), realTimeSub.getDevice());

                    this.realtimeSubscriptionMatcher.set(new SubscriptionMatcher(realTimeSub.getTable(), realTimeSub.getDevice()));
                    return outbound.sendString(realtimeSinks.asFlux().map(this.pointGson::toJson)).then();
                }).then();
    }

    /**
     * 处理设备状态 websocket 消息推送
     */
    Publisher<Void> handleDeviceStateWebsocket(WebsocketInbound inbound, WebsocketOutbound outbound, HttpServerResponse response) {
        return inbound.receive()
                .asByteArray()
                .switchMap(bytes -> {
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    WebsocketSubscription.Subscription subscription = this.gson.fromJson(
                            json,
                            WebsocketSubscription.Subscription.class
                    );
                    logger.info("接收到设备状态数据订阅: tableId={}, deviceId={}",
                            subscription.getTable(), subscription.getDevice());

                    this.deviceStatusSubscriptionMatcher.set(new SubscriptionMatcher(subscription.getTable(), subscription.getDevice()));
                    List<WebsocketSubscription.DeviceStatusMessagePayload> deviceStatues = this.createDeviceStatuses(subscription.getTable(), subscription.getDevice());
                    return outbound.sendString(
                            Flux.concat(Flux.fromArray(deviceStatues.toArray()).map(this.gson::toJson),
                                    deviceStatusSinks.asFlux().map(this.gson::toJson))
                    ).then();
                }).then();
    }

    @Override
    public void writePoint(Point point) {
        String deviceKey = String.format("%s#%s", point.getTable(), point.getId());
        WebsocketSubscription.DeviceStatusMessagePayload deviceStatusPayload = deviceStatus.computeIfAbsent(deviceKey, key -> WebsocketSubscription.DeviceStatusMessagePayload.of(point.getTable(), point.getId()));

        boolean online = true;
        long time = point.getTime();
        if (time == 0) {
            time = System.currentTimeMillis();
        } else {
            Optional<Duration> timeout = this.getDeviceNetworkTimeout(point.getTable(), point.getId());
            if (timeout.isPresent()) {
                int timeoutMs = (int) timeout.get().toMillis();
                if (System.currentTimeMillis() - time > timeoutMs) {
                    online = false;
                }
            }
        }

        if (online) {
            deviceStatusPayload.toOnline(time);
        }

        SubscriptionMatcher deviceStatusMatcher = this.deviceStatusSubscriptionMatcher.get();
        if (deviceStatusMatcher != null && deviceStatusMatcher.isMatch(point)) {
            deviceStatusSinks.tryEmitNext(deviceStatusPayload);
        }

        SubscriptionMatcher realtimeMatcher = this.realtimeSubscriptionMatcher.get();
        if (realtimeMatcher != null && realtimeMatcher.isMatch(point)) {
            realtimeSinks.tryEmitNext(point);
        }
    }

    /**
     * 定时检测设备的状态
     */
    void checkDeviceStatus() {
        while (true) {
            for (WebsocketSubscription.DeviceStatusMessagePayload deviceStatus : this.deviceStatus.values()) {
                if (!deviceStatus.isOnline()) {
                    continue;
                }

                Optional<Duration> timeout = this.getDeviceNetworkTimeout(deviceStatus.getTable(), deviceStatus.getId());
                if (timeout.isEmpty()) {
                    continue;
                }

                int timeoutMs = (int) timeout.get().toMillis();
                if (System.currentTimeMillis() - deviceStatus.getLastSeen() > timeoutMs) {
                    deviceStatus.toOffline();
                    this.deviceStatusSinks.tryEmitNext(deviceStatus);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    Optional<Duration> getDeviceNetworkTimeout(String tableId, String deviceId) {
        Optional<DeviceInfo<?>> deviceInfo = this.globalContext.getDevice(tableId, deviceId);
        if (deviceInfo.isEmpty()) {
            return Optional.empty();
        }

        DeviceInfo<?> device = deviceInfo.get();
        if (device.getSettings() == null || device.getSettings().getNetwork() == null) {
            return Optional.empty();
        }

        Double timeout = device.getSettings().getNetwork().getTimeout();
        if (timeout == null) {
            return Optional.empty();
        }

        return Optional.of(Duration.ofMillis((int) (timeout * 1000)));
    }


    List<WebsocketSubscription.DeviceStatusMessagePayload> createDeviceStatuses(String tableId, String deviceId) {
        if (!StringUtils.hasText(tableId)) {
            throw new IllegalArgumentException("未指定表");
        }

        List<WebsocketSubscription.DeviceStatusMessagePayload> payloads = new ArrayList<>();
        if (StringUtils.hasText(deviceId)) {
            String key = String.format("%s#%s", tableId, deviceId);
            payloads.add(this.deviceStatus.computeIfAbsent(key, k -> WebsocketSubscription.DeviceStatusMessagePayload.of(tableId, deviceId)));
        } else {
            List<DeviceInfo<? extends Tag>> devices = this.globalContext.getTableDevices(tableId);
            for (DeviceInfo<? extends Tag> device : devices) {
                String key = String.format("%s#%s", tableId, deviceId);
                payloads.add(this.deviceStatus.computeIfAbsent(key, k -> WebsocketSubscription.DeviceStatusMessagePayload.of(tableId, device.getId())));
            }
        }

        return payloads;
    }

    static class SubscriptionMatcher {
        private final Function<Point, Boolean> matcher;

        public SubscriptionMatcher(String tableId, String deviceId) {
            if (!StringUtils.hasText(tableId) && !StringUtils.hasText(deviceId)) {
                this.matcher = point -> true;
            } else if (StringUtils.hasText(deviceId)) {
                this.matcher = point -> point.getTable().equals(tableId) && point.getId().equals(deviceId);
            } else {
                this.matcher = point -> point.getTable().equals(tableId);
            }
        }

        boolean isMatch(Point point) {
            return this.matcher.apply(point);
        }
    }
}