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


import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;

/**
 * 驱动基础配置
 */
@Validated
@ConfigurationProperties(prefix = "airiot.driver")
public class DriverAppProperties implements InitializingBean, EnvironmentAware {

    private Environment environment;

    /**
     * 本地驱动配置文件
     */
    private boolean dataFileEnabled = false;
    private String dataFilePath = "./data.json";

    /**
     * AI 服务配置
     */
    private boolean aiServerEnabled = false;
    private String aiServerHost = "0.0.0.0";
    private int aiServerPort = 8080;

    /**
     * 当前驱动实例所属项目ID, 默认由平台注入
     */
    @NotBlank(message = "项目ID不能为空")
    @Value("${project:default}")
    private String projectId;
    /**
     * 驱动的ID
     */
    @NotBlank(message = "驱动ID不能为空")
    private String id;
    /**
     * 驱动的名称
     */
    @NotBlank(message = "驱动名称不能为空")
    private String name;
    /**
     * 驱动实例ID
     * <br>
     * 在平台安装驱动时, 该信息由平台通过命令行参数 {@code serviceId} 传入
     */
//    @NotBlank(message = "驱动实例ID不能为空")
    @Value("${serviceId:}")
    private String instanceId;

    private String distributed = "";

    public boolean isDataFileEnabled() {
        return dataFileEnabled;
    }

    public String getDataFilePath() {
        return dataFilePath;
    }

    public boolean isAiServerEnabled() {
        return aiServerEnabled;
    }

    public String getAiServerHost() {
        return aiServerHost;
    }

    public int getAiServerPort() {
        return aiServerPort;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

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

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getDistributed() {
        return distributed;
    }

    public void setDistributed(String distributed) {
        this.distributed = distributed;
    }

    @Override
    public String toString() {
        return "DriverAppProperties{" +
                "environment=" + environment +
                ", dataFileEnabled=" + dataFileEnabled +
                ", dataFilePath='" + dataFilePath + '\'' +
                ", aiServerEnabled=" + aiServerEnabled +
                ", aiServerHost='" + aiServerHost + '\'' +
                ", aiServerPort=" + aiServerPort +
                ", projectId='" + projectId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", distributed='" + distributed + '\'' +
                '}';
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.environment instanceof ConfigurableEnvironment env) {
            Optional<PropertySource<?>> propertySource = env.getPropertySources().stream()
                    .filter(ps -> ps instanceof CommandLinePropertySource)
                    .findAny();
            if (propertySource.isPresent()) {
                PropertySource<?> ps = propertySource.get();
                Object serviceId = ps.getProperty("serviceId");
                Object projectId = ps.getProperty("project");
                if (serviceId != null) {
                    this.instanceId = String.valueOf(serviceId);
                }
                if (projectId != null) {
                    this.projectId = String.valueOf(projectId);
                }
            }
        }

        this.dataFileEnabled = environment.getProperty("DATAFILE.ENABLE", Boolean.class, false);
        this.dataFilePath = environment.getProperty("DATAFILE.PATH", "./data.json");

        this.aiServerEnabled = environment.getProperty("HTTP.ENABLE", Boolean.class, false);
        this.aiServerHost = environment.getProperty("HTTP.HOST", "0.0.0.0");
        this.aiServerPort = environment.getProperty("HTTP.PORT", Integer.class, 8080);

        if (this.aiServerEnabled && !StringUtils.hasText(this.instanceId)) {
            this.instanceId = UUID.randomUUID().toString();
        }
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }
}
