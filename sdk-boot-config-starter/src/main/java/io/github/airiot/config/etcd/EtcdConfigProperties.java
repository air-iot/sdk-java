package io.github.airiot.config.etcd;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(EtcdConfigProperties.PREFIX)
public class EtcdConfigProperties implements InitializingBean, EnvironmentAware {

    public static final String PREFIX = "airiot.config.etcd";

    private Environment environment;

    /**
     * 是否启用 etcd 配置初始化功能
     */
    public boolean enabled = true;
    /**
     * etcd 服务器地址列表
     */
    private List<String> endpoints = new ArrayList<>();
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 配置信息 在 etcd 中的 key.
     */
    private String configKey = "/airiot/config/pro.json";
    /**
     * 连接超时
     */
    private Duration connectTimeout = Duration.ofSeconds(5);
    /**
     * 请求超时
     */
    private Duration readTimeout = Duration.ofSeconds(5);
    /**
     * 最大重试延迟
     */
    private Duration retryMax = Duration.ofSeconds(15);
    /**
     * 重试次数
     */
    private int retryTimes = 3;
    /**
     * 检查配置变化间隔
     */
    private Duration checkInterval = Duration.ofSeconds(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
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

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getRetryMax() {
        return retryMax;
    }

    public void setRetryMax(Duration retryMax) {
        this.retryMax = retryMax;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(Duration checkInterval) {
        this.checkInterval = checkInterval;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String etcdPassword = this.environment.getProperty("ETCD.PASSWORD");
        if(StringUtils.hasText(etcdPassword)) {
            this.password = etcdPassword;
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
