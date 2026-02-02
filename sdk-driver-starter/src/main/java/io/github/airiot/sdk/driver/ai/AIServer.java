package io.github.airiot.sdk.driver.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.github.airiot.sdk.driver.DriverApp;
import io.github.airiot.sdk.driver.DriverModules;
import io.github.airiot.sdk.driver.DriverUtils;
import io.github.airiot.sdk.driver.data.DataDispatcher;
import io.github.airiot.sdk.driver.data.DataDispatchers;
import io.github.airiot.sdk.driver.data.PointSerializationAdapter;
import io.github.airiot.sdk.driver.listener.BatchCmd;
import io.github.airiot.sdk.driver.listener.Cmd;
import io.github.airiot.sdk.driver.listener.Result;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.logger.LoggerContext;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;


/**
 * AI 扩展服务接口
 */
public class AIServer implements DisposableBean, DataDispatcher {

    private final static String CONFIG_FILE = "data.json";
    private final static String INDEX_HTML_FILE = "index.html";

    private final Logger logger = LoggerFactory.withContext().getDynamicLogger(AIServer.class);

    private final Gson pointGson = new GsonBuilder()
            .registerTypeAdapterFactory(PointSerializationAdapter.newFactory())
            .create();

    private final Gson gson = new Gson();
    private final AIServerProperties properties;
    private final DriverApp<Object, Object, Object> driverApp;
    private final DataDispatchers dispatchers;

    private final Type configType;
    private final Type cmdType;
    private final Type tagType;
    private final AtomicReference<byte[]> indexHtmlBytes = new AtomicReference<>(null);
    private final AtomicReference<RealTimeSubscription> realtimeSubscription = new AtomicReference<>(null);
    private final Sinks.Many<Point> sinks = Sinks.many().multicast().directBestEffort();

    private DisposableServer httpServer;
    private Thread watchThread;

    public AIServer(AIServerProperties properties,
                    DriverApp<Object, Object, Object> driverApp,
                    DataDispatchers dispatchers) {
        this.properties = properties;
        this.driverApp = driverApp;
        this.dispatchers = dispatchers;
        Type[] parameterizedTypes = DriverUtils.parseDriverAppGenericTypes(driverApp);
        this.configType = parameterizedTypes[0];
        this.cmdType = parameterizedTypes[1];
        this.tagType = parameterizedTypes[2];
    }

    void watchConfigFile() {
        Path cwd = Paths.get("").toAbsolutePath();
        logger.info("监听配置文件变化: {}", cwd);
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            cwd.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
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

                        if (!CONFIG_FILE.equalsIgnoreCase(targetFile)) {
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
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.info("监听配置文件变化被终止");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("监听配置文件变化事件异常, {}", cwd, e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    void init() throws Exception {
        if (this.watchThread != null) {
            this.watchThread.interrupt();
        }

        this.watchThread = new Thread(this::watchConfigFile, "config-watcher");
        this.watchThread.setDaemon(true);
        this.watchThread.start();

        this.dispatchers.register(this);

        this.startServer();
    }

    public void destroy() {
        logger.info("关闭 AIServer");

        if (this.watchThread != null) {
            this.watchThread.interrupt();
            this.watchThread = null;
        }

        if (this.httpServer != null) {
            this.httpServer.disposeNow(Duration.ofSeconds(1));
            this.httpServer = null;
        }
    }

    void startServer() throws IOException {
        String host = StringUtils.hasText(this.properties.getHost()) ? this.properties.getHost() : "0.0.0.0";
        logger.info("启动 AIServer, listen on {}:{}", host, this.properties.getPort());
        Path indexHtml = Paths.get(INDEX_HTML_FILE);
        this.httpServer = HttpServer.create()
                .port(this.properties.getPort())
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
                })
                .bindNow();

        logger.info("AIServer 已启动, listening on {}:{}", host, this.properties.getPort());
    }

    /**
     * 获取当前驱动的配置
     */
    Publisher<Void> getConfig(HttpServerRequest request, HttpServerResponse response) {
        Path configFile = Paths.get(CONFIG_FILE);
        return response.sendFile(configFile);
    }

    /**
     * 获取当前驱动的配置
     */
    Publisher<Void> saveConfig(HttpServerRequest request, HttpServerResponse response) {
        return request.receive().asString(StandardCharsets.UTF_8).flatMap(config -> {
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                fos.write(config.getBytes(StandardCharsets.UTF_8));
                return response.sendObject(Mono.just(StatusResult.of("OK")));
            } catch (Exception e) {
                return response.status(400).sendObject(Mono.just(ErrorResult.of(e.getMessage())));
            }
        });
    }

    /**
     * 从配置文件加载驱动配置
     */
    Object loadConfig() {
        try (Reader reader = new FileReader(CONFIG_FILE)) {
            return this.gson.fromJson(reader, this.configType);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException("读取驱动配置文件失败", e);
        }
    }

    /**
     * 从配置文件加载驱动配置并重启驱动
     */
    void startDriver() {
        Object config = this.loadConfig();
        this.driverApp.start(config);
    }

    /**
     * 重启驱动
     */
    Publisher<Void> handleStart(HttpServerRequest request, HttpServerResponse response) {
        return request.receive().asString(StandardCharsets.UTF_8).flatMap(config -> {
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                fos.write(config.getBytes(StandardCharsets.UTF_8));
                this.startDriver();
            } catch (FileNotFoundException ignore) {
            } catch (Exception e) {
                return response.status(400).sendObject(Mono.just(ErrorResult.of(e.getMessage())));
            }
            return response.sendObject(Mono.just(StatusResult.of("OK")));
        });
    }

    /**
     * 下发指令
     */
    Publisher<Void> handleRun(HttpServerRequest request, HttpServerResponse response) {
        LoggerContext loggerContext = LoggerContexts.push();
        loggerContext.setModule(DriverModules.RUN);
        return request.receive().asString(StandardCharsets.UTF_8).flatMap(cmdData -> {
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

            return response.sendObject(Mono.just(result));
        });
    }

    /**
     * 批量下发指令
     */
    Publisher<Void> handleBatchRun(HttpServerRequest request, HttpServerResponse response) {
        LoggerContext loggerContext = LoggerContexts.push();
        loggerContext.setModule(DriverModules.RUN);
        return request.receive().asString(StandardCharsets.UTF_8).flatMap(cmdData -> {
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

            return response.sendObject(Mono.just(result));
        });
    }

    /**
     * 写数据点
     */
    Publisher<Void> handleWriteTag(HttpServerRequest request, HttpServerResponse response) {
        LoggerContext loggerContext = LoggerContexts.push();
        loggerContext.setModule(DriverModules.RUN);
        return request.receive().asString(StandardCharsets.UTF_8).flatMap(cmdData -> {
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

            return response.sendObject(Mono.just(result));
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
        return response.header(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
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
        }
        return response.status(400).sendObject(Mono.just(ErrorResult.of("不支持的类型: " + requestType)));
    }

    /**
     * 处理 ws 请求
     */
    Publisher<Void> handleRealtimeWebsocket(WebsocketInbound inbound, WebsocketOutbound outbound, HttpServerResponse response) {
        // 后续接收到的数据都转换为 RealTimeSubscription 类型
        return inbound.receive()
                .asByteArray()
                .flatMap(bytes -> {
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    WebsocketSubscription.RealTimeSubscription realTimeSub = this.gson.fromJson(
                            json,
                            WebsocketSubscription.RealTimeSubscription.class
                    );
                    logger.info("接收到实时数据订阅: tableId={}, deviceId={}",
                            realTimeSub.getTable(), realTimeSub.getDevice());

                    this.realtimeSubscription.set(new RealTimeSubscription(realTimeSub.getTable(), realTimeSub.getDevice()));
                    return outbound.sendString(sinks.asFlux().map(this.pointGson::toJson)).then();
                }).then();
    }

    @Override
    public void writePoint(Point point) {
        RealTimeSubscription subscription = this.realtimeSubscription.get();
        if (subscription != null && subscription.isMatch(point)) {
            sinks.tryEmitNext(point);
        }
    }

    static class RealTimeSubscription {
        private final Function<Point, Boolean> matcher;

        public RealTimeSubscription(String tableId, String deviceId) {
            if (StringUtils.hasText(deviceId)) {
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