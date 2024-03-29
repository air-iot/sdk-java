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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import io.github.airiot.sdk.driver.GlobalContext;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverMQProperties;
import io.github.airiot.sdk.driver.data.AbstractDataSender;
import io.github.airiot.sdk.driver.data.DataHandlerChain;
import io.github.airiot.sdk.driver.data.LogSenderException;
import io.github.airiot.sdk.driver.data.warning.Warning;
import io.github.airiot.sdk.driver.data.warning.WarningRecovery;
import io.github.airiot.sdk.driver.data.warning.WarningSenderException;
import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.logger.LoggerContext;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.github.airiot.sdk.logger.driver.DriverModules;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * RabbitMQ Amqp 协议
 */
public class AmqpDataSender extends AbstractDataSender {

    private final Logger log = LoggerFactory.withContext().module(DriverModules.START).getStaticLogger(MQTTDataSender.class);
    private final DriverMQProperties.Rabbit rabbitProperties;
    private final ThreadLocal<Channel> channel = ThreadLocal.withInitial(this::createChannel);
    private Connection connection;

    private Channel createChannel() {
        if (this.connection == null) {
            throw new IllegalStateException("AmqpDataSender 未连接");
        }

        try {
            return this.connection.createChannel();
        } catch (IOException e) {
            throw new IllegalStateException("创建 Amqp Channel 异常", e);
        }
    }

    public AmqpDataSender(DriverDataProperties properties, DriverAppProperties appProperties,
                          DataHandlerChain chain,
                          DriverMQProperties.Rabbit rabbitProperties,
                          GlobalContext globalContext,
                          DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        super(properties, appProperties, globalContext, chain, driverGrpcClient);
        this.rabbitProperties = rabbitProperties;
    }

    @Override
    public void start() {
        if (this.isRunning()) {
            return;
        }

        log.info("AmqpDataSender: 开始连接, Server[{}:{}]",
                this.rabbitProperties.getHost(), this.rabbitProperties.getPort());

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.rabbitProperties.getHost());
        factory.setPort(this.rabbitProperties.getPort());
        factory.setUsername(this.rabbitProperties.getUsername());
        factory.setPassword(this.rabbitProperties.getPassword());
        factory.setTopologyRecoveryEnabled(true);

        // 自动重连
        long reconnectIntervalMs = this.rabbitProperties.getReconnectInterval().toMillis();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(Math.max(reconnectIntervalMs, 5000));

        try {
            this.connection = factory.newConnection();
            log.info("AmqpDataSender: 已连接");
        } catch (Exception e) {
            throw new IllegalStateException("AmqpDataSender: 连接失败", e);
        }
    }

    @Override
    public void stop() {
        if (this.connection == null) {
            return;
        }

        log.info("AmqpDataSender: 关闭中");
        try {
            this.connection.close(10000);
            log.info("AmqpDataSender: 已关闭");
        } catch (Exception e) {
            log.warn("AmqpDataSender: 断开连接异常:", e);
        }

        this.connection = null;
    }

    @Override
    public boolean isRunning() {
        return this.connection != null && this.connection.isOpen();
    }

    @Override
    public void doWritePoint(Point point) throws Exception {
        byte[] payload = this.encode(point);
        String routingKey = String.format("data.%s.%s.%s", this.projectId, point.getTable(), point.getId());
        channel.get().basicPublish("data", routingKey,
                false, false, MessageProperties.TEXT_PLAIN, payload);
    }

    @Override
    public void doWriteLog(String tableId, String deviceId, String level, String message) throws LogSenderException {
        if (!this.isRunning()) {
            throw new LogSenderException(tableId, deviceId, level, message, "未连接或连接中断");
        }

        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        try {
            this.connection.createChannel().basicPublish("logs",
                    String.format("logs.%s.%s.%s.%s", this.projectId, level, tableId, deviceId),
                    false, false,
                    MessageProperties.TEXT_PLAIN, payload);
        } catch (Exception e) {
            throw new LogSenderException(tableId, deviceId, level, message, e);
        }
    }

    @Override
    public void sendWarning(Warning warning) throws WarningSenderException {
        if (warning == null) {
            throw new WarningSenderException("报警信息不能为空");
        }

        if (!this.isRunning()) {
            throw new WarningSenderException("未连接或连接中断");
        }

        String tableId = warning.getTable().getId();
        String deviceId = warning.getTableData().getId();

        byte[] warningData = warningGson.toJson(warning).getBytes(StandardCharsets.UTF_8);

        LoggerContext context = LoggerContexts.push();
        context.withTable(tableId);
        warningLogger.info("发送报警信息, table = {}, device = {}, {}", tableId, deviceId, warning);

        try {
            this.connection.createChannel().basicPublish("warning",
                    String.format("warningStorage.%s.%s.%s", this.projectId, tableId, deviceId),
                    false, false,
                    MessageProperties.TEXT_PLAIN, warningData);
            warningLogger.info("发送报警信息完成, table = {}, device = {}, {}", tableId, deviceId, warning);
        } catch (Exception e) {
            warningLogger.warn("报警信息发送失败, table = {}, device = {}, {}", tableId, deviceId, warning, e);
            throw new WarningSenderException("报警信息发送失败", e);
        } finally {
            LoggerContexts.pop();
        }
    }

    @Override
    public void recoverWarning(String tableId, String deviceId, WarningRecovery recovery) throws WarningSenderException {
        if (!StringUtils.hasText(tableId) || !StringUtils.hasText(deviceId)) {
            throw new WarningSenderException("产生报警的设备编号及所属表标识不能为空");
        }

        if (recovery == null) {
            throw new WarningSenderException("报警恢复信息不能为空");
        }

        if (!this.isRunning()) {
            throw new WarningSenderException("未连接或连接中断");
        }

        byte[] warningData = warningGson.toJson(recovery).getBytes(StandardCharsets.UTF_8);

        LoggerContext context = LoggerContexts.push();
        context.withTable(tableId);
        warningLogger.info("发送报警恢复信息, table = {}, device = {}, {}", tableId, deviceId, recovery);

        try {
            this.connection.createChannel().basicPublish("warning",
                    String.format("warningUpdate.%s.%s.%s", this.projectId, tableId, deviceId),
                    false, false,
                    MessageProperties.TEXT_PLAIN, warningData);
            warningLogger.info("发送报警恢复信息完成, table = {}, device = {}, {}", tableId, deviceId, recovery);
        } catch (Exception e) {
            warningLogger.warn("发送报警恢复信息失败, table = {}, device = {}, {}", tableId, deviceId, recovery, e);
            throw new WarningSenderException("报警信息发送失败", e);
        } finally {
            LoggerContexts.pop();
        }
    }
}
