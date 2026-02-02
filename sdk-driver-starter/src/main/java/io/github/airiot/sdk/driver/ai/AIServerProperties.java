package io.github.airiot.sdk.driver.ai;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "airiot.driver.ai")
public class AIServerProperties {

    private boolean enabled = false;
    private String host = "0.0.0.0";
    private int port = 11211;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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
}
