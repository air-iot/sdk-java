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

package io.github.airiot.sdk.driver.config;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 驱动启动配置信息
 */
public class DriverConfig<DriverConfig, ModelConfig, DeviceConfig> {

    /**
     * 驱动实例ID
     */
    private String id;
    /**
     * 驱动名称
     */
    private String name;
    /**
     * 驱动类型
     */
    private String driverType;
    /**
     * 驱动实例配置信息
     */
    @SerializedName("device")
    private DriverConfig config;
    /**
     * 驱动下模型列表
     */
    private List<Model<ModelConfig, DeviceConfig>> tables;

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

    public String getDriverType() {
        return driverType;
    }

    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }

    public DriverConfig getConfig() {
        return config;
    }

    public void setConfig(DriverConfig config) {
        this.config = config;
    }

    public List<Model<ModelConfig, DeviceConfig>> getTables() {
        return tables;
    }

    public void setTables(List<Model<ModelConfig, DeviceConfig>> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "DriverConfig{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", driverType='" + driverType + '\'' +
                ", config=" + config +
                ", tables=" + tables +
                '}';
    }

}
