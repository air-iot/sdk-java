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

import io.github.airiot.sdk.driver.GlobalContext;
import io.github.airiot.sdk.driver.configuration.properties.DriverAppProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import io.github.airiot.sdk.driver.configuration.properties.DriverMQProperties;
import io.github.airiot.sdk.driver.data.AbstractDataSender;
import io.github.airiot.sdk.driver.data.DataHandlerChain;
import io.github.airiot.sdk.driver.data.LogSenderException;
import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.github.airiot.sdk.logger.driver.DriverModules;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * MQTT 协议
 */
public class MQTTDataSender extends AbstractDataSender implements MqttCallbackExtended {

    private final Logger log = LoggerFactory.withContext().module(DriverModules.START).getStaticLogger(MQTTDataSender.class);

    private final DriverAppProperties driverAppProperties;
    private final DriverMQProperties.Mqtt mqttProperties;
    private final int qos;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private MqttConnectOptions options;
    private MqttClient mqttClient;

    public MQTTDataSender(DataHandlerChain chain,
                          DriverDataProperties properties,
                          DriverAppProperties driverAppProperties,
                          DriverMQProperties.Mqtt mqttProperties,
                          GlobalContext globalContext,
                          DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        super(properties, driverAppProperties, globalContext, chain, driverGrpcClient);
        this.driverAppProperties = driverAppProperties;
        this.mqttProperties = mqttProperties;
        this.qos = mqttProperties.getQos();
        
        this.options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(this.mqttProperties.getUsername());
        options.setPassword(this.mqttProperties.getPassword().toCharArray());
        options.setMqttVersion(this.mqttProperties.getProtocolVersion());

        // 连接超时
        int connectTimeout = (int) this.mqttProperties.getConnectTimeout().getSeconds();
        options.setConnectionTimeout(Math.min(5, connectTimeout));

        // 心跳检测
        int keepalive = (int) this.mqttProperties.getKeepalive().getSeconds();
        options.setKeepAliveInterval(keepalive <= 0 ? 60 : keepalive);

        // 自动重连
        int reconnectIntervalMs = (int) this.mqttProperties.getReconnectInterval().toMillis();
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay(Math.max(reconnectIntervalMs, 5000));
    }

    @Override
    public void start() {
        if (!this.running.compareAndSet(false, true)) {
            log.info("MQTTDataSender: 已启动");
            return;
        }

        this.mqttClient = this.createClient();

        try {
            this.mqttClient.connect(options);
        } catch (MqttException e) {
            log.error("MQTTDataSender: 连接失败", e);
            Thread connectTask = new Thread(this::connectTask);
            connectTask.setDaemon(true);
            connectTask.setName("MQTTDataSender-ConnectTask");
            connectTask.start();
        }
    }

    private MqttClient createClient() {
        String broker = "tcp://" + this.mqttProperties.getHost() + ":" + this.mqttProperties.getPort();
        String clientId = "sdk_" + this.driverAppProperties.getId() + "_" + this.driverAppProperties.getInstanceId();

        MemoryPersistence persistence = new MemoryPersistence();

        log.info("MQTTDataSender: 客户端配置, {}", options);

        try {
            MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
            mqttClient.setTimeToWait(this.mqttProperties.getActionTimeout().toMillis());
            mqttClient.setCallback(this);
            return mqttClient;
        } catch (MqttException e) {
            throw new IllegalStateException("MQTTDataSender: 初始化失败", e);
        }
    }

    private void connectTask() {
        int reconnectIntervalMs = (int) this.mqttProperties.getReconnectInterval().toMillis();
        int retryTimes = 1;
        while (true) {
            if (!this.running.get()) {
                return;
            }

            if (this.mqttClient == null) {
                log.info("MQTTDataSender: MQTTClient is null, running = {}", this.running.get());
                return;
            }

            log.info("MQTTDataSender: 第 {} 次重试", retryTimes);

            try {
                this.mqttClient.connect(this.options);
                return;
            } catch (MqttException e) {
                // 如果当前已连接
                int code = e.getReasonCode();
                if (code == MqttException.REASON_CODE_CLIENT_CONNECTED) {
                    log.info("MQTTDataSender: 已连接(" + code + ")");
                    return;
                }

                if (code == MqttException.REASON_CODE_CONNECT_IN_PROGRESS) {
                    try {
                        this.mqttClient.close();
                    } catch (MqttException e1) {
                        log.warn("MQTTDataSender: 断开当前连接", e1);
                    }

                    try {
                        this.mqttClient.disconnectForcibly(5000);
                    } catch (MqttException e1) {
                        log.warn("MQTTDataSender: 断开当前连接", e1);
                    }

                    this.mqttClient = this.createClient();
                }

                log.error("MQTTDataSender: 第 {} 次重连失败, 下次尝试时间[{}]", retryTimes,
                        LocalDateTime.now().plus(reconnectIntervalMs, ChronoUnit.MILLIS), e);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(reconnectIntervalMs);
            } catch (InterruptedException e) {
                log.info("MQTTDataSender: 重连被终止");
                return;
            }

            retryTimes++;
        }
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(true, false) || this.mqttClient == null) {
            return;
        }

        log.info("MQTTDataSender: 关闭中");
        try {
            this.mqttClient.disconnect(10000);
            this.mqttClient.close();
            log.info("MQTTDataSender: 已关闭");
        } catch (MqttException e) {
            log.warn("MQTTDataSender: 关闭发生异常", e);
        }

        this.mqttClient = null;
    }

    @Override
    public boolean isRunning() {
        return this.mqttClient != null && this.mqttClient.isConnected();
    }

    @Override
    public void doWritePoint(Point point) throws Exception {
        this.checkRunState();
        byte[] payload = this.encode(point);
        String topic = String.format("data/%s/%s/%s", this.projectId, point.getTable(), point.getId());
        this.mqttClient.publish(topic, payload, this.qos, false);
    }

    @Override
    public void doWriteLog(String tableId, String deviceId, String level, String message) {
        if (!this.isRunning()) {
            throw new LogSenderException(tableId, deviceId, level, message, "未连接或连接中断");
        }

        try {
            this.mqttClient.publish(String.format("logs/%s/%s/%s/%s", this.projectId, level, tableId, deviceId), message.getBytes(StandardCharsets.UTF_8), this.qos, false);
        } catch (MqttException e) {
            throw new LogSenderException(tableId, deviceId, level, message, e);
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTTDataSender: 已连接");
    }

    @Override
    public void connectionLost(Throwable e) {
        log.error("MQTTDataSender: 连接断开", e);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // 未订阅任何 topic, 所以不会收到信息
        log.info("MQTTDataSender: 接收到数据, Topic[{}], {}", topic, new String(message.getPayload(), StandardCharsets.UTF_8));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (log.isDebugEnabled()) {
            MqttMessage message;
            try {
                message = token.getMessage();
                log.debug("MQTTDataSender: 数据已发送, MessageId[{}], Qos[{}], Payload[{}]",
                        message.getId(), message.getQos(),
                        new String(message.getPayload(), StandardCharsets.UTF_8));
            } catch (MqttException e) {
                log.warn("MQTTDataSender 获取已发送数据异常:", e);
            }
        }
    }
}
