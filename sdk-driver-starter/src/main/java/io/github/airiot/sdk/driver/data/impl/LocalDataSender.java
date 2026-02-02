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

package io.github.airiot.sdk.driver.data.impl;

import io.github.airiot.sdk.driver.DriverModules;
import io.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import io.github.airiot.sdk.driver.data.AbstractDataWriter;
import io.github.airiot.sdk.driver.data.DataDispatchers;
import io.github.airiot.sdk.driver.data.DataSenderException;
import io.github.airiot.sdk.driver.data.warning.Warning;
import io.github.airiot.sdk.driver.data.warning.WarningRecovery;
import io.github.airiot.sdk.driver.data.warning.WarningSenderException;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.logger.LoggerContext;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * MQTT 协议
 */
public class LocalDataSender extends AbstractDataWriter {

    private final Logger log = LoggerFactory.withContext().module(DriverModules.WRITE_POINTS).getStaticLogger(LocalDataSender.class);
    private final String projectId;

    public LocalDataSender(String projectId, DriverDataProperties properties) {
        super(properties);
        this.projectId = projectId;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void writePoint(Point point) throws DataSenderException {
        byte[] payload = this.encode(point);
        String topic = String.format("data/%s/%s/%s", this.projectId, point.getTable(), point.getId());
        log.info("WritePoint: topic={}, data={}", topic, new String(payload, StandardCharsets.UTF_8));
    }

    @Override
    public void writeLog(String tableId, String deviceId, String level, String message) {
        log.info("WriteLog: topic={}, data={}", String.format("logs/%s/%s/%s/%s", this.projectId, level, tableId, deviceId), message);
    }

    @Override
    public void sendWarning(Warning warning) throws WarningSenderException {
        if (warning == null) {
            throw new WarningSenderException("报警信息不能为空");
        }

        String tableId = warning.getTable().getId();
        String deviceId = warning.getTableData().getId();

        byte[] warningData = warningGson.toJson(warning).getBytes(StandardCharsets.UTF_8);

        LoggerContext context = LoggerContexts.push();
        context.withTable(tableId);
        warningLogger.info("发送报警信息, table = {}, device = {}, {}", tableId, deviceId, warning);

        log.info("SendWarning: topic={}, data={}", String.format("warningStorage/%s/%s/%s",
                this.projectId, warning.getTable().getId(), warning.getTableData().getId()), new String(warningData, StandardCharsets.UTF_8));
    }

    @Override
    public void recoverWarning(String tableId, String deviceId, WarningRecovery recovery) throws WarningSenderException {
        if (!StringUtils.hasText(tableId) || !StringUtils.hasText(deviceId)) {
            throw new WarningSenderException("产生报警的设备编号及所属表标识不能为空");
        }

        if (recovery == null) {
            throw new WarningSenderException("报警恢复信息不能为空");
        }

        byte[] warningData = warningGson.toJson(recovery).getBytes(StandardCharsets.UTF_8);

        LoggerContext context = LoggerContexts.push();
        context.withTable(tableId);
        warningLogger.info("发送报警恢复信息, table = {}, device = {}, {}", tableId, deviceId, recovery);

        log.info("RecoverWarning: topic={}, data={}", String.format("warningUpdate/%s/%s/%s", this.projectId, tableId, deviceId), new String(warningData, StandardCharsets.UTF_8));
    }
}
