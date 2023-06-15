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

package io.github.airiot.sdk.client.http.configuration;


import io.github.airiot.sdk.client.http.config.ServiceConfig;
import io.github.airiot.sdk.client.http.config.ServiceType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "airiot.client.http")
public class HttpClientProperties {

    private String host = "http://traefik:80";
    
    /**
     * 服务默认配置
     */
    @NestedConfigurationProperty
    private ServiceConfig defaultConfig = new ServiceConfig();

    /**
     * 服务配置列表
     */
    @NestedConfigurationProperty
    private Map<ServiceType, ServiceConfig> services = new HashMap<>();

    public ServiceConfig getOrDefault(ServiceType serviceType) {
        return this.services.getOrDefault(serviceType, this.defaultConfig);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ServiceConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(ServiceConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public Map<ServiceType, ServiceConfig> getServices() {
        return services;
    }

    public void setServices(Map<ServiceType, ServiceConfig> services) {
        this.services = services;
    }
}
