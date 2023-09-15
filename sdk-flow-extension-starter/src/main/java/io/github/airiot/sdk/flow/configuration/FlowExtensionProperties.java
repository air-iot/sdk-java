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

package io.github.airiot.sdk.flow.configuration;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 流程插件配置类
 */
@ConfigurationProperties(prefix = "flow-engine")
public class FlowExtensionProperties {
    /**
     * 流程引擎服务地址
     */
    private String host = "flow-engine";
    /**
     * 流程引擎服务端口.
     * <br>
     * 默认端: 2333
     */
    private int port = 2333;
    /**
     * 与流程引擎服务建立连接超时时间
     */
    private Duration connectTimeout = Duration.ofSeconds(15);
    /**
     * 与流程引擎服务建立连接重试间隔
     */
    private Duration retryInterval = Duration.ofSeconds(30);
    /**
     * 心跳间隔
     */
    private Duration heartbeatInterval = Duration.ofSeconds(30);
    /**
     * 最大线程数量.
     * 如果为 0 则取当前主机的 CPU 核心数量
     */
    private int maxThreads = 0;

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

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Duration retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Duration getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Duration heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
    
    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
}
