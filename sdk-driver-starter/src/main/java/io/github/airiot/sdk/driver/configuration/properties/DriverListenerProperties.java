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


import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.time.Duration;


/**
 * 驱动事件监听器配置
 */
@ConfigurationProperties(prefix = "driver-grpc")
public class DriverListenerProperties implements EnvironmentAware, InitializingBean {

    private Environment environment;

    private boolean enabled = true;
    private String host = "driver";
    private int port = 9224;
    
    /**
     * 最大接收消息大小
     * <br>
     * 单位: 字节, 默认: 4 * 1024 * 1024
     */
    private int maxInboundMessageSize = 1024 * 1024 * 64;
    private Duration keepalive = Duration.ofSeconds(30);
    private Duration reconnectInterval = Duration.ofSeconds(15);
    /**
     * 指令处理线程池最大线程数.
     * <br>
     * 如果为 0 则为 CPU 核心数
     */
    private int runMaxThreads = 0;
    /**
     * 指令处理线程池队列大小
     */
    private int runQueueSize = 32;
    /**
     * 指令结果发送队列大小
     */
    private int runResultQueueSize = 1024;

    public boolean isEnabled() {
        return enabled;
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

    public int getRunMaxThreads() {
        return runMaxThreads;
    }

    public void setRunMaxThreads(int runMaxThreads) {
        this.runMaxThreads = runMaxThreads;
    }

    public int getRunQueueSize() {
        return runQueueSize;
    }

    public void setRunQueueSize(int runQueueSize) {
        this.runQueueSize = runQueueSize;
    }

    public int getRunResultQueueSize() {
        return runResultQueueSize;
    }

    public void setRunResultQueueSize(int runResultQueueSize) {
        this.runResultQueueSize = runResultQueueSize;
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.enabled = this.environment.getProperty("DRIVERGRPC.ENABLE", Boolean.class, true);
        this.host = this.environment.getProperty("DRIVERGRPC.HOST", "127.0.0.1");
        this.port = this.environment.getProperty("DRIVERGRPC.PORT", Integer.class, 9224);
    }
}
