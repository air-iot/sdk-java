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
import io.github.airiot.sdk.driver.data.warning.Warning;
import io.github.airiot.sdk.driver.data.warning.WarningRecovery;
import io.github.airiot.sdk.driver.data.warning.WarningSenderException;
import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.logger.LoggerContext;
import io.github.airiot.sdk.logger.LoggerContexts;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.github.airiot.sdk.logger.driver.DriverModules;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * MQTT 协议
 */
public class MultiClientMQTTDataSender extends AbstractDataSender {

    private final Logger log = LoggerFactory.withContext().module(DriverModules.START).getStaticLogger(MultiClientMQTTDataSender.class);

    private final DriverAppProperties driverAppProperties;
    private final DriverMQProperties.Mqtt mqttProperties;
    private final int qos;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final MqttConnectOptions options;
    private final List<MqttClient> mqttClients;
    private final Map<Integer, MqttClient> availableClients;

    public MultiClientMQTTDataSender(DataHandlerChain chain,
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
        options.setHttpsHostnameVerificationEnabled(this.mqttProperties.isSslVerification());

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

        this.mqttClients = new ArrayList<>(mqttProperties.getClients());
        for (int i = 0; i < mqttProperties.getClients(); i++) {
            String clientId = "sdk_" + this.driverAppProperties.getId() + "_" + this.driverAppProperties.getInstanceId() + "_" + i;
            this.mqttClients.add(this.createClient(i, clientId, mqttProperties.isSsl()));
        }

        this.availableClients = new ConcurrentHashMap<>(this.mqttClients.size());
    }

    @Override
    public void start() {
        log.info("MultiClientMQTTDataSender: 启动");
        if (!this.running.compareAndSet(false, true)) {
            log.info("MultiClientMQTTDataSender: 已启动");
            return;
        }

        log.info("MultiClientMQTTDataSender: 连接中, 共 {} 个客户端", this.mqttClients.size());

        for (int i = 0; i < this.mqttClients.size(); i++) {
            MqttClient mqttClient = this.mqttClients.get(i);
            try {
                log.info("MultiClientMQTTDataSender: 连接中, {}", mqttClient.getClientId());
                mqttClient.connect(options);
                log.info("MultiClientMQTTDataSender: 连接成功, {}", mqttClient.getClientId());
            } catch (MqttException e) {
                log.error("MultiClientMQTTDataSender: 连接失败", e);
                Thread connectTask = new Thread(() -> this.connectTask(mqttClient));
                connectTask.setDaemon(true);
                connectTask.setName("MQTTDataSender-ConnectTask");
                connectTask.start();
            }
        }
    }

    private MqttClient createClient(int index, String clientId, boolean ssl) {
        String broker = (ssl ? "ssl" : "tcp") + "://" + this.mqttProperties.getHost() + ":" + this.mqttProperties.getPort();

        MemoryPersistence persistence = new MemoryPersistence();

        log.info("MultiClientMQTTDataSender: 客户端配置, {}, {}", broker, options);

        try {
            MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
            mqttClient.setManualAcks(false);
            mqttClient.setCallback(new CustomMqttCallbackExtended(index, mqttClient));
            return mqttClient;
        } catch (MqttException e) {
            throw new IllegalStateException("MultiClientMQTTDataSender: 初始化失败", e);
        }
    }

    private void connectTask(MqttClient mqttClient) {
        int reconnectIntervalMs = (int) this.mqttProperties.getReconnectInterval().toMillis();
        int retryTimes = 1;
        while (true) {
            if (!this.running.get()) {
                return;
            }

            log.info("MultiClientMQTTDataSender: 第 {} 次重试, {}", retryTimes, mqttClient.getClientId());

            try {
                mqttClient.connect(this.options);
                log.info("MultiClientMQTTDataSender: 第 {} 次重试, 连接成功, {}", retryTimes, mqttClient.getClientId());
                return;
            } catch (MqttException e) {
                // 如果当前已连接
                int code = e.getReasonCode();
                if (code == MqttException.REASON_CODE_CLIENT_CONNECTED) {
                    log.info("MultiClientMQTTDataSender: 已连接({}), {}", code, mqttClient.getClientId());
                    return;
                }

                if (code == MqttException.REASON_CODE_CONNECT_IN_PROGRESS) {
                    try {
                        mqttClient.close();
                    } catch (MqttException e1) {
                        log.warn("MultiClientMQTTDataSender: 断开当前连接, {}", mqttClient.getClientId(), e1);
                    }

                    try {
                        mqttClient.disconnectForcibly(5000);
                    } catch (MqttException e1) {
                        log.warn("MultiClientMQTTDataSender: 断开当前连接, {}", mqttClient.getClientId(), e1);
                    }
                }

                log.error("MultiClientMQTTDataSender: 第 {} 次重连失败, 下次尝试时间[{}], {}", retryTimes,
                        LocalDateTime.now().plus(reconnectIntervalMs, ChronoUnit.MILLIS), mqttClient.getClientId(), e);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(reconnectIntervalMs);
            } catch (InterruptedException e) {
                log.info("MultiClientMQTTDataSender: 重连被终止, {}", mqttClient.getClientId());
                return;
            }

            retryTimes++;
        }
    }

    @Override
    public void stop() {
        log.info("MultiClientMQTTDataSender: 停止");
        if (!this.running.compareAndSet(true, false)) {
            log.info("MultiClientMQTTDataSender: 未启动");
            return;
        }

        log.info("MultiClientMQTTDataSender: 关闭中");

        for (MqttClient mqttClient : this.mqttClients) {
            if (mqttClient != null) {
                try {
                    mqttClient.disconnect(10000);
                    mqttClient.close();
                } catch (MqttException e) {
                    log.warn("MultiClientMQTTDataSender: 关闭发生异常", e);
                }
            }
        }

        log.info("MultiClientMQTTDataSender: 已关闭");

        this.mqttClients.clear();
        this.availableClients.clear();
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    protected void checkRunState() {
        if (!this.running.get()) {
            log.warn("MultiClientMQTTDataSender: 未启动, 手动启动");
            this.start();
            throw new IllegalStateException("当前未启动, 手动启动中");
        }

        if (this.availableClients.isEmpty()) {
            throw new IllegalStateException("未连接到 MQTT 服务器或连接已断开");
        }
    }

    void publish(String topic, byte[] payload) throws Exception {
        if (this.availableClients.isEmpty()) {
            throw new IllegalStateException("未连接到 MQTT 服务器");
        }

        int size = this.mqttClients.size();
        int index = (int) (System.currentTimeMillis() % size);
        for (int i = 0; i < size; i++) {
            MqttClient client = this.availableClients.get(index);
            if (client != null && client.isConnected()) {
                client.publish(topic, payload, this.qos, false);
                return;
            }
            index = (index + 1) % size;
        }
        throw new IllegalStateException("未找到可用的客户端");
    }

    @Override
    public void doWritePoint(Point point) throws Exception {
        this.checkRunState();
        byte[] payload = this.encode(point);
        String topic = String.format("data/%s/%s/%s", this.projectId, point.getTable(), point.getId());
        this.publish(topic, payload);
    }

    @Override
    public void doWriteLog(String tableId, String deviceId, String level, String message) {
        this.checkRunState();
        try {
            this.publish(String.format("logs/%s/%s/%s/%s", this.projectId, level, tableId, deviceId), message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new LogSenderException(tableId, deviceId, level, message, e);
        }
    }

    @Override
    public void sendWarning(Warning warning) throws WarningSenderException {
        if (warning == null) {
            throw new WarningSenderException("报警信息不能为空");
        }

        this.checkRunState();

        String tableId = warning.getTable().getId();
        String deviceId = warning.getTableData().getId();

        byte[] warningData = warningGson.toJson(warning).getBytes(StandardCharsets.UTF_8);

        LoggerContext context = LoggerContexts.push();
        context.withTable(tableId);
        warningLogger.info("发送报警信息, table = {}, device = {}, {}", tableId, deviceId, warning);

        try {
            this.publish(String.format("warningStorage/%s/%s/%s", this.projectId, warning.getTable().getId(), warning.getTableData().getId()), warningData);
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

        this.checkRunState();

        byte[] warningData = warningGson.toJson(recovery).getBytes(StandardCharsets.UTF_8);

        LoggerContext context = LoggerContexts.push();
        context.withTable(tableId);
        warningLogger.info("发送报警恢复信息, table = {}, device = {}, {}", tableId, deviceId, recovery);

        try {
            this.publish(String.format("warningUpdate/%s/%s/%s", this.projectId, tableId, deviceId), warningData);
            warningLogger.info("发送报警恢复信息完成, table = {}, device = {}, {}", tableId, deviceId, recovery);
        } catch (Exception e) {
            warningLogger.warn("发送报警恢复信息失败, table = {}, device = {}, {}", tableId, deviceId, recovery, e);
            throw new WarningSenderException("报警恢复信息发送失败", e);
        } finally {
            LoggerContexts.pop();
        }
    }

    class CustomMqttCallbackExtended implements MqttCallbackExtended {

        private final MqttClient client;
        private final Integer index;

        public CustomMqttCallbackExtended(Integer index, MqttClient client) {
            this.index = index;
            this.client = client;
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            log.info("MultiClientMQTTDataSender: 已连接, {}", this.client.getClientId());
            MultiClientMQTTDataSender.this.availableClients.put(this.index, client);
        }

        @Override
        public void connectionLost(Throwable cause) {
            log.error("MultiClientMQTTDataSender: 连接断开, {}", this.client.getClientId(), cause);
            MultiClientMQTTDataSender.this.availableClients.remove(this.index);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    }
}
