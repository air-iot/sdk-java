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


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


/**
 * 驱动事件监听器配置
 */
@ConfigurationProperties(prefix = "driver-grpc")
public class DriverListenerProperties {

    private String host = "driver";
    private int port = 9224;
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
}
