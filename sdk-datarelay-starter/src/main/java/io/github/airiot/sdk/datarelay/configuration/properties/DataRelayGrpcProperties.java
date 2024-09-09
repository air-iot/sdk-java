package io.github.airiot.sdk.datarelay.configuration.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "data-relay-grpc")
public class DataRelayGrpcProperties {

    /**
     * 北向服务管理服务地址
     */
    private String host = "data-relay";
    /**
     * 北向服务管理服务端口
     */
    private int port = 9232;
    /**
     * 心跳检查间隔
     */
    private Duration healthCheckInterval = Duration.ofSeconds(30);
    /**
     * 心跳重试次数
     */
    private int healthMaxRetryTimes = 3;

    /**
     * 最大接收消息大小
     * <br>
     * 单位: 字节, 默认: 4 * 1024 * 1024
     */
    private int maxInboundMessageSize = 1024 * 1024 * 4;
    private Duration keepalive = Duration.ofSeconds(30);
    private Duration reconnectInterval = Duration.ofSeconds(15);

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

    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public void setHealthCheckInterval(Duration healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }

    public int getHealthMaxRetryTimes() {
        return healthMaxRetryTimes;
    }

    public void setHealthMaxRetryTimes(int healthMaxRetryTimes) {
        this.healthMaxRetryTimes = healthMaxRetryTimes;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
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
