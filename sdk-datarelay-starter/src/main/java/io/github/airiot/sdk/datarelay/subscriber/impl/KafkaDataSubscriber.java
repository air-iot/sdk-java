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

package io.github.airiot.sdk.datarelay.subscriber.impl;

import com.google.gson.Gson;
import io.github.airiot.sdk.client.service.core.TimingDataClient;
import io.github.airiot.sdk.datarelay.DataRelayModules;
import io.github.airiot.sdk.datarelay.configuration.properties.DataRelayAppProperties;
import io.github.airiot.sdk.datarelay.configuration.properties.DataRelayMQProperties;
import io.github.airiot.sdk.datarelay.subscriber.AbstractDataSubscriber;
import io.github.airiot.sdk.datarelay.subscriber.DeviceData;
import io.github.airiot.sdk.logger.LoggerFactory;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * kafka 消息组件
 */
public class KafkaDataSubscriber extends AbstractDataSubscriber {

    private final Logger log = LoggerFactory.withContext().module(DataRelayModules.START).getStaticLogger(MQTTDataSubscriber.class);

    private final Gson gson = new Gson();
    private final ReentrantReadWriteLock subscriptionLock = new ReentrantReadWriteLock();
    private final Set<String> subscriptions = new HashSet<>(128);
    private final DataRelayAppProperties appProperties;
    private final DataRelayMQProperties.Kafka kafkaProperties;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String projectId;

    private Consumer<String, Bytes> kafkaClient;
    private Thread pollThread;

    public KafkaDataSubscriber(TimingDataClient timingDataClient, DataRelayAppProperties appProperties,
                               DataRelayMQProperties.Kafka kafkaProperties) {
        super(timingDataClient);
        this.appProperties = appProperties;
        this.projectId = appProperties.getProjectId();
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public void start() {
        if (!this.running.compareAndSet(false, true)) {
            log.info("KafkaDataSubscriber: 已启动");
            return;
        }

        String clientId = null;
        if (StringUtils.hasText(this.kafkaProperties.getClientId())) {
            clientId = this.kafkaProperties.getClientId();
        } else {
            clientId = "sdk_datarelay_" + this.appProperties.getId() + "_" + this.appProperties.getInstanceId();
        }

        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, clientId);
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", this.kafkaProperties.getBrokers()));
        configs.put(ConsumerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, (int) this.kafkaProperties.getConnectTimeout().toMillis());
        configs.put(ConsumerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG, (int) this.kafkaProperties.getConnectTimeout().toMillis() * 3);
        configs.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, (int) this.kafkaProperties.getReconnectInterval().toMillis());
        configs.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, (int) this.kafkaProperties.getReconnectInterval().toMillis() * 3);
        configs.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, this.kafkaProperties.getFetchMaxBytes());
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, this.kafkaProperties.getMaxPollRecords());
        configs.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, this.kafkaProperties.getRequestTimeout().toMillis());

        log.info("KafkaDataSubscriber: 客户端配置, {}", configs);

        this.kafkaClient = new KafkaConsumer<>(configs, new StringDeserializer(), new BytesDeserializer());
        this.kafkaClient.subscribe(Collections.singleton("data"));

        if (this.pollThread != null) {
            this.pollThread.interrupt();
        }

        this.pollThread = new Thread(this::poll);
        this.pollThread.setName("kafka-poller");
        this.pollThread.setDaemon(true);
        this.pollThread.start();
    }

    @Override
    public void stop() {
        log.info("KafkaDataSubscriber: 关闭中");
        this.running.set(false);

        if (this.kafkaClient != null) {
            try {
                this.kafkaClient.close(Duration.ofSeconds(5));
            } catch (Exception e) {
                log.warn("KafkaDataSubscriber: 关闭客户端失败, {}", e.getMessage(), e);
            }
        }

        if (this.pollThread != null) {
            this.pollThread.interrupt();
        }

        this.kafkaClient = null;
        this.pollThread = null;
        log.info("KafkaDataSubscriber: 已关闭");
    }

    @Override
    public boolean isRunning() {
        return this.running.get() && this.kafkaClient != null;
    }

    @Override
    protected void onSubscribes(List<Subscription> subscriptions) {
        Lock lock = this.subscriptionLock.writeLock();
        try {
            List<String> newSubscriptions = subscriptions.stream()
                    .map(s -> this.deviceKey(s.tableId, s.deviceId))
                    .collect(Collectors.toList());
            lock.lock();
            this.subscriptions.clear();
            this.subscriptions.addAll(newSubscriptions);
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void onClearSubscriptions(List<Subscription> subscriptions) {
        Lock lock = this.subscriptionLock.writeLock();
        lock.lock();
        this.subscriptions.clear();
        lock.unlock();
    }

    /**
     * 开始拉取数据
     */
    private void poll() {
        Duration timeout = this.kafkaProperties.getPollTimeout();
        Lock lock = this.subscriptionLock.readLock();
        while (this.running.get()) {
            try {
                ConsumerRecords<String, Bytes> records = this.kafkaClient.poll(timeout);
                for (ConsumerRecord<String, Bytes> record : records) {
                    String key = record.key();
                    String[] fields = key.split("/");
                    if (fields.length != 3) {
                        log.warn("KafkaDataSubscriber: 接收到的数据的 key 格式不正确, {}", key);
                        continue;
                    }

                    String projectId = fields[0];
                    String tableId = fields[1];
                    String deviceId = fields[2];

                    if (!this.projectId.equals(projectId)) {
                        log.trace("KafkaDataSubscriber: 接收到数据, key 为 '{}', 与当前项目 '{}' 不匹配", key, this.projectId);
                        continue;
                    }

                    String payload = record.value().toString();

                    log.debug("KafkaDataSubscriber: 表={},设备={}. 接收到数据, key '{}', payload '{}'", tableId, deviceId, key, payload);

                    String devKey = this.deviceKey(tableId, deviceId);

                    lock.lock();
                    if (!this.subscriptions.contains(devKey)) {
                        lock.unlock();
                        continue;
                    }

                    try {
                        DeviceData data = this.gson.fromJson(payload, DeviceData.class);
                        this.fill(tableId, deviceId, data);
                        super.deliver(data);
                    } catch (Exception e) {
                        log.error("KafkaDataSubscriber: 表={},设备={}. 解析 payload '{}' 失败", tableId, deviceId, payload, e);
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (Exception e) {
                log.error("KafkaDataSubscriber: poll 数据异常", e);
            }
        }
    }


    private String deviceKey(String tableId, String deviceId) {
        return String.format("#T%s#D%s", tableId, deviceId);
    }
}
