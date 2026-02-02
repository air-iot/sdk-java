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
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.driver.DeviceInfo;
import io.github.airiot.sdk.driver.DriverModules;
import io.github.airiot.sdk.driver.GlobalContext;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.data.warning.Warning;
import io.github.airiot.sdk.driver.data.warning.WarningRecovery;
import io.github.airiot.sdk.driver.data.warning.WarningSenderException;
import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import io.github.airiot.sdk.driver.grpc.driver.Request;
import io.github.airiot.sdk.driver.grpc.driver.Response;
import io.github.airiot.sdk.driver.grpc.driver.TableDataRequest;
import io.github.airiot.sdk.driver.model.*;
import io.github.airiot.sdk.logger.LoggerContext;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 默认数据发送实现类
 */
public class DefaultDataSender implements DataSender {

    private final Logger writePointLogger = LoggerFactory.withContext().module(DriverModules.WRITE_POINTS).getDynamicLogger(DefaultDataSender.class);
    private final Logger writeEventLogger = LoggerFactory.withContext().module(DriverModules.WRITE_EVENT).getStaticLogger(DefaultDataSender.class);

    private final DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    protected final Gson gson = new Gson();

    protected final String projectId;
    protected final String driverId;
    protected final String serviceId;
    private final DataHandlerChain chain;
    private final GlobalContext globalContext;
    private final DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient;
    private final DataWriter dataWriter;
    private final DataDispatchers dispatchers;

    public DefaultDataSender(DriverAppProperties appProperties,
                             GlobalContext globalContext,
                             DataHandlerChain chain,
                             DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient,
                             DataWriter dataWriter,
                             DataDispatchers dispatchers) {
        this.projectId = appProperties.getProjectId();
        this.driverId = appProperties.getId();
        this.serviceId = appProperties.getInstanceId();
        this.globalContext = globalContext;
        this.chain = chain;
        this.driverGrpcClient = driverGrpcClient;
        this.dataWriter = dataWriter;
        this.dispatchers = dispatchers;
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
                .setTableId(tableId)
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

        this.dispatchers.writePoint(newPoint);

        try {
            this.dataWriter.writePoint(newPoint);
        } catch (Exception e) {
            writePointLogger.error("上报数据异常, point = {}", newPoint, e);
            throw new DataSenderException(point, "上报数据异常", e);
        }
    }

    @Override
    public void logDebug(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "debug", this.formatLog(tableId, deviceId, this.formatLog(tableId, deviceId, msg)));
    }

    @Override
    public void logInfo(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "info", this.formatLog(tableId, deviceId, this.formatLog(tableId, deviceId, msg)));
    }

    @Override
    public void logWarn(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "warn", this.formatLog(tableId, deviceId, this.formatLog(tableId, deviceId, msg)));
    }

    @Override
    public void logError(String tableId, String deviceId, String msg) throws LogSenderException {
        this.doWriteLog(tableId, deviceId, "error", this.formatLog(tableId, deviceId, msg));
    }

    void doWriteLog(String tableId, String deviceId, String level, String msg) {
        this.dispatchers.writeLog(tableId, deviceId, level, msg);
        this.dataWriter.writeLog(tableId, deviceId, level, msg);
    }

    @Override
    public void sendWarning(Warning warning) throws WarningSenderException {
        this.dataWriter.sendWarning(warning);
    }

    @Override
    public void recoverWarning(String tableId, String deviceId, WarningRecovery recovery) throws WarningSenderException {
        this.dataWriter.recoverWarning(tableId, deviceId, recovery);
    }

    private String formatLog(String tableId, String deviceId, String msg) {
        return "{\"time\":\"" + LocalDateTime.now().format(logTimeFormatter) + "\",\"message\":\"" + msg + "\",\"tableId\":\"" + tableId + "\",\"deviceId\":\"" + deviceId + "\"}";
    }

    @Override
    public void start() {
        this.dataWriter.start();
    }

    @Override
    public void stop() {
        this.dataWriter.stop();
    }

    @Override
    public boolean isRunning() {
        return this.dataWriter.isRunning();
    }
}
