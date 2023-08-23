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

package io.github.airiot.sdk.algorithm.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.UUID;

@Validated
@ConfigurationProperties(prefix = "algorithm")
public class AlgorithmProperties {

    /**
     * 算法标识
     */
    @NotBlank(message = "算法ID不能为空")
    private String id;
    /**
     * 算法名称
     */
    @NotBlank(message = "算法名称不能为空")
    private String name;
    /**
     * 算法服务ID(实例ID)
     */
    @Value("${serviceId:}")
    private String serviceId;
    /**
     * 最大线程线量.
     * 如果为 0, 则取当前机器的CPU核数
     */
    private int maxThreads = 0;
    /**
     * 重连的间隔时间
     */
    private Duration reconnectInterval = Duration.ofSeconds(15);
    /**
     * 心跳检查的间隔时间
     */
    private Duration keepaliveInterval = Duration.ofSeconds(30);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getServiceId() {
        if (serviceId == null || serviceId.isEmpty()) {
            this.serviceId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Duration getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(Duration reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public Duration getKeepaliveInterval() {
        return keepaliveInterval;
    }

    public void setKeepaliveInterval(Duration keepaliveInterval) {
        this.keepaliveInterval = keepaliveInterval;
    }
}
