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
import io.github.airiot.sdk.driver.data.DataSenderException;
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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * kafka 消息组件
 */
public class KafkaDataSender extends AbstractDataSender {

    private final Logger log = LoggerFactory.withContext().module(DriverModules.START).getStaticLogger(MQTTDataSender.class);

    private final DriverAppProperties driverAppProperties;
    private final DriverMQProperties.Kafka kafkaProperties;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Integer partition;
    private Producer<String, Bytes> kafkaClient;

    public KafkaDataSender(DriverDataProperties properties,
                           DriverAppProperties driverAppProperties,
                           DriverMQProperties.Kafka kafkaProperties,
                           GlobalContext globalContext, DataHandlerChain chain,
                           DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        super(properties, driverAppProperties, globalContext, chain, driverGrpcClient);
        this.driverAppProperties = driverAppProperties;
        this.kafkaProperties = kafkaProperties;
        this.partition = kafkaProperties.getPartition();
    }

    @Override
    public void start() {
        if (!this.running.compareAndSet(false, true)) {
            log.info("KafkaDataSender: 已启动");
            return;
        }

        String clientId = null;
        if (StringUtils.hasText(this.kafkaProperties.getClientId())) {
            clientId = this.kafkaProperties.getClientId();
        } else {
            clientId = "sdk_" + this.driverAppProperties.getId() + "_" + this.driverAppProperties.getInstanceId();
        }

        int deliverTimeoutMs = (int) this.kafkaProperties.getDeliverTimeout().toMillis();

        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", this.kafkaProperties.getBrokers()));
        configs.put(ProducerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, (int) this.kafkaProperties.getConnectTimeout().toMillis());
        configs.put(ProducerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG, (int) this.kafkaProperties.getConnectTimeout().toMillis() * 3);
        configs.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, (int) this.kafkaProperties.getReconnectInterval().toMillis());
        configs.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, (int) this.kafkaProperties.getReconnectInterval().toMillis() * 3);
        configs.put(ProducerConfig.LINGER_MS_CONFIG, 500);
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, 100);
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, deliverTimeoutMs / 3);
        configs.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliverTimeoutMs);

        log.info("KafkaDataSender: 客户端配置, {}", configs);

        this.kafkaClient = new KafkaProducer<>(configs, new StringSerializer(), new BytesSerializer());
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(true, false) || this.kafkaClient == null) {
            return;
        }

        log.info("KafkaDataSender: 关闭中");

        try {
            this.kafkaClient.close(Duration.ofSeconds(5));
        } catch (Exception e) {
            log.warn("KafkaDataSender: 关闭客户端失败, {}", e.getMessage(), e);
        }

        this.kafkaClient = null;
        log.info("KafkaDataSender: 已关闭");
    }

    @Override
    public void doWritePoint(Point point) throws Exception {
        byte[] payload = this.encode(point);
        String key = String.format("%s/%s/%s", this.projectId, point.getTable(), point.getId());
        ProducerRecord<String, Bytes> record = null;
        if (this.partition == null) {
            record = new ProducerRecord<>("data", key, new Bytes(payload));
        } else {
            record = new ProducerRecord<>("data", this.partition, key, new Bytes(payload));
        }

        try {
            this.kafkaClient.send(record).get();
        } catch (Exception e) {
            throw new DataSenderException(point, "发送数据失败", e);
        }
    }

    @Override
    public void doWriteLog(String tableId, String deviceId, String level, String message) throws LogSenderException {
        String key = String.format("%s/%s/%s/%s", this.projectId, level, tableId, deviceId);
        ProducerRecord<String, Bytes> record = new ProducerRecord<>("logs", key, new Bytes(message.getBytes()));

        try {
            this.kafkaClient.send(record).get();
        } catch (Exception e) {
            throw new LogSenderException(tableId, deviceId, level, message, e);
        }
    }

    @Override
    public boolean isRunning() {
        return this.kafkaClient != null;
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

        String key = String.format("%s/%s/%s", this.projectId, tableId, deviceId);
        ProducerRecord<String, Bytes> record = new ProducerRecord<>("warningStorage", key, new Bytes(warningData));

        try {
            this.kafkaClient.send(record).get();
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

        String key = String.format("%s/%s/%s", this.projectId, tableId, deviceId);
        ProducerRecord<String, Bytes> record = new ProducerRecord<>("warningUpdate", key, new Bytes(warningData));

        try {
            this.kafkaClient.send(record).get();
            warningLogger.info("发送报警恢复信息完成, table = {}, device = {}, {}", tableId, deviceId, recovery);
        } catch (Exception e) {
            warningLogger.warn("发送报警恢复信息失败, table = {}, device = {}, {}", tableId, deviceId, recovery, e);
            throw new WarningSenderException("报警恢复信息发送失败", e);
        } finally {
            LoggerContexts.pop();
        }
    }
}
