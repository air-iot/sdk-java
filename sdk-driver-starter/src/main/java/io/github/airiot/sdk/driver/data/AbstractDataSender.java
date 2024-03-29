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

package io.github.airiot.sdk.driver.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.driver.DeviceInfo;
import io.github.airiot.sdk.driver.DriverModules;
import io.github.airiot.sdk.driver.GlobalContext;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import io.github.airiot.sdk.driver.grpc.driver.Request;
import io.github.airiot.sdk.driver.grpc.driver.Response;
import io.github.airiot.sdk.driver.grpc.driver.TableDataRequest;
import io.github.airiot.sdk.driver.model.*;
import io.github.airiot.sdk.logger.LoggerContext;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.logging.LogLevel;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public abstract class AbstractDataSender implements DataSender, InitializingBean {

    private final Logger writePointLogger = LoggerFactory.withContext().module(DriverModules.WRITE_POINTS).getDynamicLogger(AbstractDataSender.class);
    private final Logger writeEventLogger = LoggerFactory.withContext().module(DriverModules.WRITE_EVENT).getStaticLogger(AbstractDataSender.class);
    protected final Logger warningLogger = LoggerFactory.withContext().module(DriverModules.WARNING).getStaticLogger(AbstractDataSender.class);

    private final DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    protected final Gson gson = new Gson();
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
    /**
     * {@link Point} 序列化专用对象
     */
    private final Gson pointSerialization = new GsonBuilder()
            .registerTypeAdapterFactory(PointSerializationAdapter.newFactory())
            .create();

    protected final String projectId;
    protected final String driverId;
    protected final String serviceId;
    private final DataHandlerChain chain;
    private final GlobalContext globalContext;
    private final DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient;

    private final DriverDataProperties properties;

    private Consumer<Point> dataHandlerOnConnectionLost;

    public AbstractDataSender(DriverDataProperties properties, DriverAppProperties appProperties, GlobalContext globalContext,
                              DataHandlerChain chain,
                              DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        this.properties = properties;
        this.projectId = appProperties.getProjectId();
        this.driverId = appProperties.getId();
        this.serviceId = appProperties.getInstanceId();
        this.globalContext = globalContext;
        this.chain = chain;
        this.driverGrpcClient = driverGrpcClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        switch (properties.getPolicy()) {
            case LOG:
                this.dataHandlerOnConnectionLost = this.createLogHandler(properties.getLogLevel());
                break;
            case EXCEPTION:
                this.dataHandlerOnConnectionLost = this::throwDataSenderExceptionHandler;
                break;
            case DISCARD:
                this.dataHandlerOnConnectionLost = this::discardHandler;
                break;
            default:
                throw new IllegalArgumentException("未定义的连接断开时的数据处理策略: " + properties.getPolicy());
        }
    }

    private Consumer<Point> createLogHandler(LogLevel level) {
        return point -> {
            String deviceId = point.getId();
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(point.getTime()), ZoneId.systemDefault());
            Map<String, Object> tagValues = new HashMap<>();
            for (Field<? extends Tag> field : point.getFields()) {
                if (field == null || field.getTag() == null || !StringUtils.hasText(field.getTag().getId())) {
                    writePointLogger.warn("上报数据: 数据上报失败, 连接已断开. 数据点信息不正确, deviceId = {}, field = {}", deviceId, field);
                    continue;
                }
                tagValues.put(field.getTag().getId(), field.getValue());
            }

            if (tagValues.isEmpty()) {
                writePointLogger.warn("上报数据: 数据上报失败, 连接已断开. 数据点信息不正确, {}", point);
                return;
            }

            switch (level) {
                case TRACE:
                    writePointLogger.trace("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case DEBUG:
                    writePointLogger.debug("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case INFO:
                    writePointLogger.info("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case WARN:
                    writePointLogger.warn("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case ERROR:
                case FATAL:
                    writePointLogger.error("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
            }
        };
    }

    private void throwDataSenderExceptionHandler(Point point) {
        throw new DataSenderException(point, "数据上报失败, 服务未启动或连接已断开");
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

    private void discardHandler(Point point) {
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
        writePointLogger.trace("上报数据: 连接断开, 丢弃数据. Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
    }

    /**
     * 对采集到的数据编码
     *
     * @param point 采集到的数据信息
     * @return 编码后的字节数组
     */
    protected byte[] encode(Point point) {
        return pointSerialization.toJson(point).getBytes(StandardCharsets.UTF_8);
    }

    protected boolean setTableIfAbsent(String deviceId, Consumer<String> setter) {
        Optional<DeviceInfo<? extends Tag>> deviceInfo = this.globalContext.getDevice(deviceId);
        if (!deviceInfo.isPresent()) {
            return false;
        }
        setter.accept(deviceInfo.get().getTableId());
        return true;
    }

    @Override
    public Response writeEvent(Event event) throws EventSenderException {
        if (!this.isRunning()) {
            throw new EventSenderException(event, "未连接或连接中断");
        }

        writeEventLogger.debug("MQTTDataSender: 发送事件, {} ", event);
        if (event.getId().isEmpty() || event.getEventId().isEmpty()) {
            writeEventLogger.warn("MQTTDataSender: 发送事件, 缺少资产ID或事件ID, {}", event);
            throw new EventSenderException(event, "非法的事件, 缺少资产ID或事件ID");
        }

        // 如果未定义 table 则设置该信息
        if (!StringUtils.hasText(event.getTable()) && !setTableIfAbsent(event.getId(), event::setTable)) {
            throw new EventSenderException(event, "非法请求, 未找到设备对应的表: " + event.getId());
        }

        try {
            return this.driverGrpcClient.event(
                    Request.newBuilder()
                            .setProject(this.projectId)
                            .setData(ByteString.copyFrom(gson.toJson(event, Event.class), StandardCharsets.UTF_8))
                            .build()
            );
        } catch (Exception e) {
            writeEventLogger.error("MQTTDataSender: 发送事件失败, {}", event, e);
            throw new EventSenderException(event, e);
        }
    }

    @Override
    public Response writeRunLog(RunLog runLog) throws RunLogSenderException {
        if (!this.isRunning()) {
            throw new RunLogSenderException(runLog, "未连接或连接中断");
        }

        try {
            return this.driverGrpcClient.commandLog(
                    Request.newBuilder()
                            .setProject(this.projectId)
                            .setData(ByteString.copyFrom(gson.toJson(runLog, RunLog.class), StandardCharsets.UTF_8))
                            .build()
            );
        } catch (Exception e) {
            throw new RunLogSenderException(runLog, e);
        }
    }


    protected Response internalFindTableData(String tableId, String deviceId) {
        TableDataRequest request = TableDataRequest.newBuilder()
                .setProjectId(this.projectId)
                .setDriverId(this.driverId)
                .setService(this.serviceId)
                .setTableDataId(tableId)
                .setTableDataId(deviceId)
                .build();
        Response response = this.driverGrpcClient.findTableData(request);
        if (!response.getStatus()) {
            throw new QueryTableDataException(response.getCode(), response.getInfo(), response.getDetail());
        }
        return response;
    }

    @Override
    public <T> T findTableData(Class<T> tClass, String tableId, String deviceId) {
        Response response = this.internalFindTableData(tableId, deviceId);
        if (response.getResult().isEmpty()) {
            return null;
        }

        if (tClass == String.class) {
            return (T) response.getResult().toStringUtf8();
        }

        return gson.fromJson(response.getResult().toStringUtf8(), tClass);
    }

    @Override
    public <T> T findTableData(TypeToken<T> tClass, String tableId, String deviceId) throws QueryTableDataException {
        Response response = this.internalFindTableData(tableId, deviceId);
        if (response.getResult().isEmpty()) {
            return null;
        }

        return gson.fromJson(response.getResult().toStringUtf8(), tClass);
    }

    @Override
    public Map<String, Object> findTableData(String tableId, String deviceId) {
        return this.findTableData(MAP_TYPE_TOKEN, tableId, deviceId);
    }

    @Override
    public Response updateTableData(UpdateTableDTO tableDTO) throws UpdateTableDataException {
        Assert.hasText(tableDTO.getRowId(), "未提供有效的设备编号");
        Assert.notEmpty(tableDTO.getFields(), "未提供有效的更新字段");

        // 如果未定义 table 则设置该信息
        if (!StringUtils.hasText(tableDTO.getTable()) && !setTableIfAbsent(tableDTO.getRowId(), tableDTO::setTable)) {
            throw new UpdateTableDataException(tableDTO, "非法请求, 未找到设备对应的表: " + tableDTO.getRowId());
        }

        try {
            return this.driverGrpcClient.updateTableData(
                    Request.newBuilder()
                            .setProject(this.projectId)
                            .setData(ByteString.copyFrom(gson.toJson(tableDTO, UpdateTableDTO.class), StandardCharsets.UTF_8))
                            .build()
            );
        } catch (Exception e) {
            throw new UpdateTableDataException(tableDTO, e);
        }
    }

    @Override
    public void writePoint(String tableId, String deviceId, long time, Map<String, Object> tagValues) {
        Point point = this.globalContext.createPoint(tableId, deviceId, time, tagValues);
        LoggerContext context = LoggerContexts.push();
        context.withTable(tableId);
        try {
            this.writePoint(point);
        } finally {
            LoggerContexts.pop();
        }
    }

    @Override
    public void writePoint(Point point) {
        writePointLogger.debug("上报数据: {}", point);

        if (CollectionUtils.isEmpty(point.getFields())) {
            writePointLogger.debug("上报数据: 无数据点信息, {}", point);
            return;
        }

        if (!this.isRunning()) {
            this.dataHandlerOnConnectionLost.accept(point);
            return;
        }

        // 如果未提供 table 信息则自动填充
        if (!StringUtils.hasText(point.getTable())) {
            Optional<DeviceInfo<? extends Tag>> deviceInfo = this.globalContext.getDevice(point.getId());
            if (!deviceInfo.isPresent()) {
                throw new DataSenderException(point, "填充 Point.table 信息失败, 未找到设备 '" + point.getId() + "' 的信息");
            }
            point.setTable(deviceInfo.get().getTableId());
        }

        String tableId = point.getTable();
        String deviceId = point.getId();

        LoggerContext context = LoggerContexts.push();
        context.withTable(tableId);

        Point newPoint = null;

        try {
            newPoint = this.chain.handle(point);
            writePointLogger.debug("采集数据处理: 原始数据: {}, 处理后数据: {}", point, newPoint);

            if (CollectionUtils.isEmpty(newPoint.getFields())) {
                writePointLogger.warn("采集数据处理: 处理后数据点列表为空, 原始数据: {}, 处理后数据: {}", point, newPoint);
                return;
            }

            if (point.getFields().size() > newPoint.getFields().size()) {
                writePointLogger.debug("采集数据处理: 数据处理后数据点数量减少, table={}, device={}, 由 {} 减少到 {}. 处理前: {}, 处理后: {}",
                        tableId, deviceId, point.getFields().size(), newPoint.getFields().size(), point, newPoint);

                // 处理后剩余的数据点列表
                Set<String> retainFields = newPoint.getFields().stream()
                        .filter(Objects::nonNull)
                        .filter(field -> field.getTag() != null)
                        .map(field -> field.getTag().getId())
                        .collect(Collectors.toSet());

                Map<String, Object> droppedFields = new HashMap<>(point.getFields().size() - newPoint.getFields().size());
                for (Field<?> field : point.getFields()) {
                    if (field == null || field.getTag() == null) {
                        writePointLogger.warn("采集数据处理: 数据点的 field 或 tag 信息为 null, point = {}, field = {}", point, field);
                        continue;
                    }

                    if (!retainFields.contains(field.getTag().getId())) {
                        droppedFields.put(field.getTag().getId(), field.getValue());
                    }
                }

                writePointLogger.warn("采集数据处理: 处理后部分数据点数据被丢弃, table={}, device={}, dropped = {}",
                        tableId, deviceId, droppedFields);
            } else if (point.getFields().size() < newPoint.getFields().size()) {
                writePointLogger.debug("采集数据处理: 数据处理后数据点数量增加, table={}, device={}, 由 {} 增加到 {}. 处理前: {}, 处理后: {}",
                        tableId, deviceId, point.getFields().size(), newPoint.getFields().size(), point, newPoint);

                // 处理前的数据点列表
                Set<String> oldFields = point.getFields().stream()
                        .filter(Objects::nonNull)
                        .filter(field -> field.getTag() != null)
                        .map(field -> field.getTag().getId())
                        .collect(Collectors.toSet());

                Map<String, Object> addedFields = new HashMap<>(newPoint.getFields().size() - point.getFields().size());
                for (Field<?> field : newPoint.getFields()) {
                    if (field == null || field.getTag() == null) {
                        writePointLogger.warn("采集数据处理: 数据点的 field 或 tag 信息为 null, point = {}, field = {}", point, field);
                        continue;
                    }

                    if (!oldFields.contains(field.getTag().getId())) {
                        addedFields.put(field.getTag().getId(), field.getValue());
                    }
                }

                writePointLogger.warn("采集数据处理: 处理后增加了数据点, table={}, device={}, added = {}", tableId, deviceId, addedFields);
            }
        } catch (Exception e) {
            writePointLogger.error("采集数据处理: 数据处理失败, point = {}", point, e);
            throw new DataSenderException(point, "数据处理失败", e);
        } finally {
            LoggerContexts.pop();
        }

        try {
            this.doWritePoint(newPoint);
        } catch (Exception e) {
            writePointLogger.error("上报数据异常, point = {}", newPoint, e);
            throw new DataSenderException(point, "上报数据异常", e);
        }
    }

    @Override
    public void logDebug(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "debug", this.formatLog(tableId, deviceId, msg));
    }

    @Override
    public void logInfo(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "info", this.formatLog(tableId, deviceId, msg));
    }

    @Override
    public void logWarn(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "warn", this.formatLog(tableId, deviceId, msg));
    }

    @Override
    public void logError(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "error", this.formatLog(tableId, deviceId, msg));
    }

    private String formatLog(String tableId, String deviceId, String msg) {
        return "{\"time\":\"" + LocalDateTime.now().format(logTimeFormatter) + "\",\"message\":\"" + msg + "\",\"tableId\":\"" + tableId + "\",\"deviceId\":\"" + deviceId + "\"}";
    }

    /**
     * 发送数据
     *
     * @param point 资产采集到的数据信息
     * @throws Exception 如果发送数据时发生异常
     */
    public abstract void doWritePoint(Point point) throws Exception;

    /**
     * 发送日志
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param level    日志级别
     * @param message  日志内容
     * @throws LogSenderException 如果发送日志时发生异常
     */
    public abstract void doWriteLog(String tableId, String deviceId, String level, String message) throws LogSenderException;
}
