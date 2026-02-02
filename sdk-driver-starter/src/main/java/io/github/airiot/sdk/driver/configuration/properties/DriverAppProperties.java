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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
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
     * 启用驱动相关功能配置项
     * <br>
     * 会影响 {@code GrpcDriverEventListener} 和 {@code DataSender}
     */
    public static final String DRIVER_ENABLE_PROPERTY = "airiot.driver.enabled";

    /**
     * 标准模式
     */
    public static final String NORMAL_MODE = "normal";
    /**
     * 本地模式
     */
    public static final String LOCAL_MODE = "local";

    /**
     * 驱动的运行模式
     * <br>
     * normal: 标准模式
     * <br>
     * local: 本地模式. 该模式下, 不会连接平台 grpc
     */
    private String mode = NORMAL_MODE;

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
    @NotBlank(message = "驱动实例ID不能为空")
    @Value("${serviceId:}")
    private String instanceId;

    private String distributed = "";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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
                "projectId='" + projectId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", distributed='" + distributed + '\'' +
                '}';
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment env = (ConfigurableEnvironment) this.environment;
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

        if(LOCAL_MODE.equalsIgnoreCase(this.mode)) {
            this.instanceId = UUID.randomUUID().toString();
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
