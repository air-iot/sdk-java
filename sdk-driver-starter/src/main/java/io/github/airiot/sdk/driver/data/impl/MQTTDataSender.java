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
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * MQTT 协议
 */
public class MQTTDataSender extends AbstractDataSender implements MqttCallbackExtended {

    private final Logger log = LoggerFactory.getLogger(MQTTDataSender.class);

    private final DriverAppProperties driverAppProperties;
    private final DriverMQProperties.Mqtt mqttProperties;
    private final int qos;
    private final long publishTimeoutMs;
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * 缓存设备ID与上报数据 topic 的映射关系
     */
    private final Map<String, String> deviceTopicCache = new ConcurrentHashMap<>();

    private ScheduledExecutorService executorService;
    private MqttClient mqttClient;
    private MqttConnectOptions options;

    public MQTTDataSender(DataHandlerChain chain,
                          DriverDataProperties properties,
                          DriverAppProperties driverAppProperties,
                          DriverMQProperties.Mqtt mqttProperties,
                          GlobalContext globalContext,
                          DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        super(properties, driverAppProperties.getProjectId(), globalContext, chain, driverGrpcClient);
        this.driverAppProperties = driverAppProperties;
        this.mqttProperties = mqttProperties;
        this.publishTimeoutMs = mqttProperties.getPublishTimeout().toMillis();
        this.qos = mqttProperties.getQos();
    }

    private Runnable wrapRunnable(Map<String, String> mdcContext, Runnable r) {
        return () -> {
            if (mdcContext != null) {
                for (Map.Entry<String, String> entry : mdcContext.entrySet()) {
                    MDC.put(entry.getKey(), entry.getValue());
                }
            }
            r.run();
        };
    }

    private ScheduledThreadPoolExecutor createScheduledThreadPoolExecutor(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize,
                r -> {
                    Thread t = new Thread(wrapRunnable(MDC.getCopyOfContextMap(), r));
                    t.setDaemon(true);
                    return t;
                });
    }

    @Override
    public void start() {
        if (!this.running.compareAndSet(false, true)) {
            log.info("MQTTDataSender: 已启动");
            return;
        }

        String broker = "tcp://" + this.mqttProperties.getHost() + ":" + this.mqttProperties.getPort();
        String clientId = "sdk_" + this.driverAppProperties.getId() + "_" + this.driverAppProperties.getInstanceId();

        log.info("MQTTDataSender: 开始连接, Broker[{}], ClientId[{}]", broker, clientId);

        MemoryPersistence persistence = new MemoryPersistence();
        options = new MqttConnectOptions();
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

        this.executorService = this.createScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

        try {
            this.mqttClient = new MqttClient(broker, clientId, persistence, this.executorService);
            this.mqttClient.setTimeToWait(this.mqttProperties.getActionTimeout().toMillis());
            this.mqttClient.setCallback(this);
        } catch (MqttException e) {
            throw new IllegalStateException("MQTTDataSender: 初始化失败", e);
        }

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

    private void connectTask() {
        int reconnectIntervalMs = (int) this.mqttProperties.getReconnectInterval().toMillis();
        int retryTimes = 1;
        while (true) {
            if (!this.running.get()) {
                return;
            }

            log.info("MQTTDataSender: 连接断开, 第 {} 次重试", retryTimes);

            try {
                this.mqttClient.connect(this.options);
                return;
            } catch (MqttException e) {
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
        } finally {
            if (this.executorService != null && !this.executorService.isShutdown() && !this.executorService.isTerminated()) {
                this.executorService.shutdown();
                this.executorService = null;
            }
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
        MqttMessage msg = new MqttMessage(payload);
        msg.setQos(this.qos);

        String deviceId = point.getId();
        String topic = deviceTopicCache.computeIfAbsent(deviceId,
                dId -> String.format("data/%s/%s/%s", this.projectId, point.getTable(), point.getId()));
        MqttTopic tp = this.mqttClient.getTopic(topic);
        tp.publish(msg).waitForCompletion(this.publishTimeoutMs);
    }

    @Override
    public void doWriteLog(String tableId, String deviceId, String level, String message) {
        if (!this.isRunning()) {
            throw new LogSenderException(tableId, deviceId, level, message, "未连接或连接中断");
        }

        MqttMessage m = new MqttMessage();
        m.setQos(0);
        m.setPayload(message.getBytes(StandardCharsets.UTF_8));
        MqttTopic tp = this.mqttClient.getTopic(String.format("logs/%s/%s/%s/%s", this.projectId, level, tableId, deviceId));
        try {
            tp.publish(m).waitForCompletion(this.publishTimeoutMs);
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
        log.info("MQTTDataSender: 连接断开");
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
