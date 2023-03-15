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

package com.github.airiot.sdk.driver.data;

import com.github.airiot.sdk.driver.DeviceInfo;
import com.github.airiot.sdk.driver.GlobalContext;
import com.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import com.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import com.github.airiot.sdk.driver.grpc.driver.Request;
import com.github.airiot.sdk.driver.grpc.driver.Response;
import com.github.airiot.sdk.driver.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.logging.LogLevel;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractDataSender implements DataSender, InitializingBean {

    private final Logger log = LoggerFactory.getLogger(AbstractDataSender.class);

    private final DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    protected final Gson gson = new Gson();
    /**
     * {@link Point} 序列化专用对象
     */
    private final Gson pointSerialization = new GsonBuilder()
            .registerTypeAdapterFactory(PointSerializationAdapter.newFactory())
            .create();

    protected final String projectId;
    private final DataHandlerChain chain;
    private final GlobalContext globalContext;
    private final DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient;

    private final DriverDataProperties properties;

    private Consumer<Point> dataHandlerOnConnectionLost;

    public AbstractDataSender(DriverDataProperties properties, String projectId, GlobalContext globalContext,
                              DataHandlerChain chain,
                              DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        this.properties = properties;
        this.projectId = projectId;
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
                    log.warn("上报数据: 数据上报失败, 连接已断开. 数据点信息不正确, deviceId = {}, field = {}", deviceId, field);
                    continue;
                }
                tagValues.put(field.getTag().getId(), field.getValue());
            }

            if (tagValues.isEmpty()) {
                log.warn("上报数据: 数据上报失败, 连接已断开. 数据点信息不正确, {}", point);
                return;
            }

            switch (level) {
                case TRACE:
                    log.trace("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case DEBUG:
                    log.debug("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case INFO:
                    log.info("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case WARN:
                    log.warn("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
                    break;
                case ERROR:
                case FATAL:
                    log.error("上报数据: 数据上报失败, 连接已断开, Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
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
        log.trace("上报数据: 连接断开, 丢弃数据. Device[{}], Time[{}], {}", deviceId, dateTime, tagValues);
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

        log.debug("MQTTDataSender: 发送事件, {} ", event);
        if (event.getId().isEmpty() || event.getEventId().isEmpty()) {
            log.warn("MQTTDataSender: 发送事件, 缺少资产ID或事件ID, {}", event);
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
            throw new EventSenderException(event, e);
        }
    }

    @Override
    public Response writeRunLog(RunLog runLog) throws RunLogSenderException {
        if (!this.isRunning()) {
            throw new RunLogSenderException(runLog, "未连接或连接中断");
        }

        try {
            return this.driverGrpcClient.event(
                    Request.newBuilder()
                            .setProject(this.projectId)
                            .setData(ByteString.copyFrom(gson.toJson(runLog, RunLog.class), StandardCharsets.UTF_8))
                            .build()
            );
        } catch (Exception e) {
            throw new RunLogSenderException(runLog, e);
        }
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
        this.writePoint(point);
    }

    @Override
    public void writePoint(Point point) {
        if (log.isDebugEnabled()) {
            log.debug("上报数据: {}", point);
        }

        if (CollectionUtils.isEmpty(point.getFields())) {
            log.debug("上报数据: 无数据点信息, {}", point);
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

        Point newPoint = null;

        try {
            newPoint = this.chain.handle(point);
            if (log.isDebugEnabled()) {
                log.debug("采集数据处理: 原始数据[{}], 处理后数据[{}]", point, newPoint);
            }
        } catch (Exception e) {
            log.error("采集数据处理: 数据处理失败, point = {}", point, e);
            throw new DataSenderException(point, "数据处理失败", e);
        }

        if (newPoint == null || CollectionUtils.isEmpty(newPoint.getFields())) {
            log.warn("采集数据处理: 无效的数据, {}", point);
            return;
        }
        
        try {
            if (!setTableIfAbsent(newPoint.getId(), newPoint::setTable)) {
                log.warn("采集数据处理: 未找到设备对应的表, device = {}", newPoint.getId());
                return;
            }

            this.doWritePoint(newPoint);
        } catch (Exception e) {
            throw new DataSenderException(point, "上报数据异常", e);
        }
    }

    @Override
    public void logDebug(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "debug", this.formatLog(deviceId, msg));
    }

    @Override
    public void logInfo(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "info", this.formatLog(deviceId, msg));
    }

    @Override
    public void logWarn(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "warn", this.formatLog(deviceId, msg));
    }

    @Override
    public void logError(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "error", this.formatLog(deviceId, msg));
    }

    private String formatLog(String deviceId, String msg) {
        return "{\"time\":\"" + LocalDateTime.now().format(logTimeFormatter) + "\",\"message\":\"" + msg + "\",\"deviceId\":\"" + deviceId + "\"}";
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
