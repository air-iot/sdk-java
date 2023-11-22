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

package io.github.airiot.sdk.driver.configuration.properties;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "mq")
public class DriverMQProperties {

    /**
     * 数据上报方式
     */
    private DataSenderType type = DataSenderType.MQTT;

    /**
     * mqtt上报数据方式相关配置, 只有 {@link #type} 为 {@link DataSenderType#MQTT} 时有效
     */
    @NestedConfigurationProperty
    private Mqtt mqtt = new Mqtt();
    /**
     * amqp 上报数据方式相关配置, 只有 {@link #type} 为 {@link DataSenderType#RABBIT} 时有效
     */
    @NestedConfigurationProperty
    private Rabbit rabbit = new Rabbit();
    /**
     * kafka 上报数据方式相关配置, 只有 {@link #type} 为 {@link DataSenderType#KAFKA} 时有效
     */
    @NestedConfigurationProperty
    private Kafka kafka = new Kafka();

    public DataSenderType getType() {
        return type;
    }

    public void setType(DataSenderType type) {
        this.type = type;
    }

    public Mqtt getMqtt() {
        return mqtt;
    }

    public void setMqtt(Mqtt mqtt) {
        this.mqtt = mqtt;
    }

    public Rabbit getAmqp() {
        return rabbit;
    }

    public void setAmqp(Rabbit rabbit) {
        this.rabbit = rabbit;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public static class Mqtt {
        private String host = "mqtt";
        private int port = 1883;
        private String username = "admin";
        private String password = "public";
        /**
         * MQTT 协议版本号
         * <br>
         * 3 => 3.1
         * <br>
         * 4 => 3.1.1
         * <br>
         * 默认为 0, 表示: 先尝试使用 {@code 3.1.1} 协议, 如果失败则切换回 {@code 3.1}
         */
        private int protocolVersion = 0;
        private int qos = 0;
        /**
         * 连接超时时间
         * <br>
         * 最短超时为 5s, 如果设置的超时时间小于 5s 则为 5s
         * <br>
         * 默认: 10s
         */
        private Duration connectTimeout = Duration.ofSeconds(10);
        /**
         * 操作超时
         *
         * @see MqttClient#getTimeToWait()
         * <br>
         * 默认: 1s
         */
        private Duration actionTimeout = Duration.ofSeconds(1);
        /**
         * 发布消息超时
         */
        private Duration publishTimeout = Duration.ofSeconds(1);
        /**
         * 心跳检测间隔
         * <br>
         * 如果小于或等于 {@code 0} 则不开启心跳检测
         * <br>
         * 默认: 60s
         */
        private Duration keepalive = Duration.ofSeconds(60);
        /**
         * 连接断开后重连间隔
         * <br>
         * 如果小于或等于 {@code 0} 则不开启重连
         */
        private Duration reconnectInterval = Duration.ofSeconds(5);

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public int getQos() {
            return qos;
        }

        public void setQos(int qos) {
            this.qos = qos;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getActionTimeout() {
            return actionTimeout;
        }

        public void setActionTimeout(Duration actionTimeout) {
            this.actionTimeout = actionTimeout;
        }

        public Duration getPublishTimeout() {
            return publishTimeout;
        }

        public void setPublishTimeout(Duration publishTimeout) {
            this.publishTimeout = publishTimeout;
        }

        public Duration getKeepalive() {
            return keepalive;
        }

        public void setKeepalive(Duration keepalive) {
            this.keepalive = keepalive;
        }

        public Duration getReconnectInterval() {
            return reconnectInterval;
        }

        public void setReconnectInterval(Duration reconnectInterval) {
            this.reconnectInterval = reconnectInterval;
        }
    }

    public static class Rabbit {

        private String host = "rabbit";
        private int port = 5672;
        private String username = "admin";
        private String password = "public";
        /**
         * 心跳检测间隔
         * <br>
         * 如果小于或等于 {@code 0} 则不开启心跳检测
         */
        private Duration keepalive = Duration.ofSeconds(10);
        /**
         * 连接断开后重连间隔
         * <br>
         * 如果小于或等于 {@code 0} 则不开启重连
         */
        private Duration reconnectInterval = Duration.ofSeconds(5);

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Duration getKeepalive() {
            return keepalive;
        }

        public void setKeepalive(Duration keepalive) {
            this.keepalive = keepalive;
        }

        public Duration getReconnectInterval() {
            return reconnectInterval;
        }

        public void setReconnectInterval(Duration reconnectInterval) {
            this.reconnectInterval = reconnectInterval;
        }
    }


    /**
     * kafka 消息组件
     */
    public static class Kafka {
        /**
         * kafka 服务地址列表
         * <br>
         * 例如:
         * <pre>
         *  mq:
         *    kafka:
         *      brokers:
         *      - 192.168.88.130:9092
         *      - 192.168.88.131:9092
         * </pre>
         */
        private List<String> brokers = new ArrayList<>();
        /**
         * 客户端 ID. 如果不填写则自动生成
         */
        private String clientId;
        /**
         * 数据发送的目标分区. 如果不填写则根据消息的 key 分配到对应的分区
         */
        private Integer partition;
        /**
         * 连接超时
         */
        private Duration connectTimeout = Duration.ofSeconds(5);
        /**
         * 重连间隔
         */
        private Duration reconnectInterval = Duration.ofSeconds(5);
        /**
         * 发布消息超时
         */
        private Duration deliverTimeout = Duration.ofSeconds(5);

        public List<String> getBrokers() {
            return brokers;
        }

        public void setBrokers(List<String> brokers) {
            this.brokers = brokers;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public Integer getPartition() {
            return partition;
        }

        public void setPartition(Integer partition) {
            this.partition = partition;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getReconnectInterval() {
            return reconnectInterval;
        }

        public void setReconnectInterval(Duration reconnectInterval) {
            this.reconnectInterval = reconnectInterval;
        }

        public Duration getDeliverTimeout() {
            return deliverTimeout;
        }

        public void setDeliverTimeout(Duration deliverTimeout) {
            this.deliverTimeout = deliverTimeout;
        }

        @Override
        public String toString() {
            return "Kafka{" +
                    "brokers=" + brokers +
                    ", clientId='" + clientId + '\'' +
                    ", partition=" + partition +
                    ", connectTimeout=" + connectTimeout +
                    ", reconnectInterval=" + reconnectInterval +
                    ", deliverTimeout=" + deliverTimeout +
                    '}';
        }
    }

    public enum DataSenderType {
        /**
         * MQTT 协议
         */
        MQTT,

        /**
         * Rabbitmq 协议
         */
        RABBIT,
        /**
         * kafka 消息组件
         */
        KAFKA,
    }
}
