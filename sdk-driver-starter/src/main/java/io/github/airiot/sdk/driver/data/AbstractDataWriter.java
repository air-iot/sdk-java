package io.github.airiot.sdk.driver.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.airiot.sdk.driver.DriverModules;
import io.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import io.github.airiot.sdk.driver.model.Field;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.driver.model.Tag;
import io.github.airiot.sdk.logger.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public abstract class AbstractDataWriter implements DataWriter {

    protected final Logger logger = LoggerFactory.withContext().module(DriverModules.WRITE_POINTS).getDynamicLogger(DataWriter.class);
    protected final Logger warningLogger = LoggerFactory.withContext().module(DriverModules.WARNING).getStaticLogger(DefaultDataSender.class);

    /**
     * {@link Point} 序列化专用对象
     */
    private final Gson pointGson = new GsonBuilder()
            .registerTypeAdapterFactory(PointSerializationAdapter.newFactory())
            .create();
    protected final Gson warningGson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
                @Override
                public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                    out.value(value.format(ISO_OFFSET_DATE_TIME));
                }

                @Override
                public ZonedDateTime read(JsonReader in) throws IOException {
                    return ZonedDateTime.parse(in.nextString(), ISO_OFFSET_DATE_TIME);
                }
            })
            .create();

    protected final Consumer<Point> dataHandlerOnConnectionLost;

    public AbstractDataWriter(DriverDataProperties properties) {
        this.dataHandlerOnConnectionLost = this.createDataHandlerOnConnectionLost(properties);
    }

    /**
     * 检查连接状态, 如果未处于运行状态则抛出异常
     *
     * @throws IllegalStateException 如果未处于运行状态
     */
    protected void checkRunState() {
        if (!this.isRunning()) {
            throw new IllegalStateException("连接未创建或已连开");
        }
    }

    Consumer<Point> createDataHandlerOnConnectionLost(DriverDataProperties properties) {
        return switch (properties.getPolicy()) {
            case LOG -> this.createLogHandler(properties.getLogLevel());
            case EXCEPTION -> this::throwDataSenderExceptionHandler;
            case DISCARD -> this::discardHandler;
            default ->
                    throw new IllegalArgumentException("未定义的连接断开时的数据处理策略: " + properties.getPolicy());
        };
    }

    Consumer<Point> createLogHandler(LogLevel level) {
        return point -> {
            String deviceId = point.getId();
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(point.getTime()), ZoneId.systemDefault());
            Map<String, Object> tagValues = new HashMap<>();
            for (Field<? extends Tag> field : point.getFields()) {
                if (field == null || field.getTag() == null || !StringUtils.hasText(field.getTag().getId())) {
                    logger.warn("上报数据: 数据上报失败, 连接已断开. 数据点信息不正确, deviceId = {}, field = {}", deviceId, field);
                    continue;
                }
                tagValues.put(field.getTag().getId(), field.getValue());
            }

            if (tagValues.isEmpty()) {
                logger.warn("上报数据: 数据上报失败, 连接已断开. 数据点信息不正确, {}", point);
                return;
            }

            switch (level) {
                case TRACE:
                    logger.trace("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case DEBUG:
                    logger.debug("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case INFO:
                    logger.info("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case WARN:
                    logger.warn("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case ERROR:
                case FATAL:
                    logger.error("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
            }
        };
    }

    void throwDataSenderExceptionHandler(Point point) {
        throw new DataSenderException(point, "数据上报失败, 服务未启动或连接已断开");
    }

    void discardHandler(Point point) {
        String deviceId = point.getId();
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(point.getTime()), ZoneId.systemDefault());
        Map<String, Object> tagValues = new HashMap<>();
        for (Field<? extends Tag> field : point.getFields()) {
            if (field == null || field.getTag() == null || !StringUtils.hasText(field.getTag().getId())) {
                continue;
            }
            tagValues.put(field.getTag().getId(), field.getValue());
        }

        if (tagValues.isEmpty()) {
            return;
        }
        logger.trace("上报数据: 连接断开, 丢弃数据. Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
    }

    /**
     * 对采集到的数据编码
     *
     * @param point 采集到的数据信息
     * @return 编码后的字节数组
     */
    protected byte[] encode(Point point) {
        return pointGson.toJson(point).getBytes(StandardCharsets.UTF_8);
    }
}
