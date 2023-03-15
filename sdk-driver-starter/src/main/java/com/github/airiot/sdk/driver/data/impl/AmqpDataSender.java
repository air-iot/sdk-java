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

package com.github.airiot.sdk.driver.data.impl;

import com.github.airiot.sdk.driver.GlobalContext;
import com.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import com.github.airiot.sdk.driver.configuration.properties.DriverMQProperties;
import com.github.airiot.sdk.driver.data.AbstractDataSender;
import com.github.airiot.sdk.driver.data.DataHandlerChain;
import com.github.airiot.sdk.driver.data.LogSenderException;
import com.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import com.github.airiot.sdk.driver.model.Point;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * RabbitMQ Amqp 协议
 */
public class AmqpDataSender extends AbstractDataSender {

    private final Logger log = LoggerFactory.getLogger(AmqpDataSender.class);
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

    public AmqpDataSender(DriverDataProperties properties, String projectId,
                          DataHandlerChain chain,
                          DriverMQProperties.Rabbit rabbitProperties,
                          GlobalContext globalContext,
                          DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        super(properties, projectId, globalContext, chain, driverGrpcClient);
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
}
