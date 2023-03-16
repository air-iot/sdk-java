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


import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * 驱动基础配置
 */
@Validated
@ConfigurationProperties(prefix = "airiot.driver")
public class DriverAppProperties implements InitializingBean {

    /**
     * 启用驱动相关功能配置项
     * <br>
     * 会影响 {@code GrpcDriverEventListener} 和 {@code DataSender}
     */
    public static final String DRIVER_ENABLE_PROPERTY = "airiot.driver.enabled";

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

    }
}
