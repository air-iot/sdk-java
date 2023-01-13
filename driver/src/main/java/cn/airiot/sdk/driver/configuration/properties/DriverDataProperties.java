package cn.airiot.sdk.driver.configuration.properties;


import cn.airiot.sdk.driver.data.DataSenderException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;

import java.time.Duration;


/**
 * 驱动采集的数据及事件上报配置
 */
@ConfigurationProperties(prefix = "airiot.driver.data")
public class DriverDataProperties {

    /**
     * 数据上报方式
     */
    private DataSenderType type = DataSenderType.MQTT;
    /**
     * 连接断开时的数据处理策略
     */
    private DataHandlePolicyOnConnectLost policy = DataHandlePolicyOnConnectLost.EXCEPTION;
    /**
     * 连接断开时, 输出到日志的等级
     * <br>
     * 只有 {@link #policy} 为 {@link DataHandlePolicyOnConnectLost#logLevel} 时有效
     */
    private LogLevel logLevel = LogLevel.ERROR;
    /**
     * mqtt上报数据方式相关配置, 只有 {@link #type} 为 {@link DataSenderType#MQTT} 时有效
     */
    private Mqtt mqtt = new Mqtt();
    /**
     * amqp 上报数据方式相关配置, 只有 {@link #type} 为 {@link DataSenderType#AMQP} 时有效
     */
    private Amqp amqp = new Amqp();

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

    public Amqp getAmqp() {
        return amqp;
    }

    public void setAmqp(Amqp amqp) {
        this.amqp = amqp;
    }

    public DataHandlePolicyOnConnectLost getPolicy() {
        return policy;
    }

    public void setPolicy(DataHandlePolicyOnConnectLost policy) {
        this.policy = policy;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public static class Mqtt {
        private String host = "mqtt";
        private int port = 1883;
        private String username = "admin";
        private String password = "public";
        private int qos = 0;
        /**
         * 连接超时时间
         * <br>
         * 最短超时为 5s, 如果设置的超时时间小于 5s 则为 5s
         */
        private Duration connectTimeout = Duration.ofSeconds(10);
        /**
         * 操作超时
         *
         * @see MqttClient#getTimeToWait()
         */
        private Duration actionTimeout = Duration.ofSeconds(10);
        /**
         * 心跳检测间隔
         * <br>
         * 如果小于或等于 {@code 0} 则不开启心跳检测
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


    public static class Amqp {

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

    public enum DataSenderType {
        MQTT,
        AMQP
    }

    /**
     * 连接断开时的数据处理策略
     */
    public enum DataHandlePolicyOnConnectLost {
        /**
         * 抛出 {@link DataSenderException} 异常
         */
        EXCEPTION,
        /**
         * 输出到日志
         */
        LOG,
        /**
         * 丢弃
         */
        DISCARD;
    }
}
