package cn.airiot.sdk.driver.configuration.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


/**
 * 驱动事件监听器配置
 */
@ConfigurationProperties(prefix = "airiot.driver.listener")
public class DriverListenerProperties {

    /**
     * 事件监听器类型
     */
    private ListenerType type = ListenerType.GRPC;
    /**
     * GRPC 事件监听器配置
     * <br>
     * 只有 {@link #type} 为 {@link ListenerType#grpc} 时有效
     */
    private Grpc grpc = new Grpc();

    public ListenerType getType() {
        return type;
    }

    public void setType(ListenerType type) {
        this.type = type;
    }

    public Grpc getGrpc() {
        return grpc;
    }

    public void setGrpc(Grpc grpc) {
        this.grpc = grpc;
    }

    public static class Grpc {
        private String host = "driver";
        private int port = 9224;
        private Duration keepalive = Duration.ofSeconds(5);
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

    public enum ListenerType {
        GRPC
    }
}
