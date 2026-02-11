package io.github.airiot.sdk.driver.config;

/**
 * 基础配置
 */
public class BasicSettings {

    private Double interval;
    private Network network;

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    /**
     * 通讯超时配置
     */
    public static class Network {
        private Double timeout;

        public Double getTimeout() {
            return timeout;
        }

        public void setTimeout(Double timeout) {
            this.timeout = timeout;
        }
    }
}
