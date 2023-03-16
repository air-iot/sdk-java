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
 * 模型配置
 *
 * @param <ModelConfig>  模型配置类泛型
 * @param <DeviceConfig> 设备配置类泛型
 */
public class Model<ModelConfig, DeviceConfig> {
    /**
     * 表标识(模型ID)
     */
    private String id;
    /**
     * 模型所属驱动实例ID
     */
    private String driverInstanceId;
    /**
     * 模型配置信息
     */
    @SerializedName("device")
    private ModelConfig config;
    private List<Device<DeviceConfig>> devices;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriverInstanceId() {
        return driverInstanceId;
    }

    public void setDriverInstanceId(String driverInstanceId) {
        this.driverInstanceId = driverInstanceId;
    }

    public ModelConfig getConfig() {
        return config;
    }

    public void setConfig(ModelConfig config) {
        this.config = config;
    }

    public List<Device<DeviceConfig>> getDevices() {
        return devices;
    }

    public void setDevices(List<Device<DeviceConfig>> devices) {
        this.devices = devices;
    }

    @Override
    public String toString() {
        return "Table{" +
                "id='" + id + '\'' +
                ", driverInstanceId='" + driverInstanceId + '\'' +
                ", config=" + config +
                ", devices=" + devices +
                '}';
    }
}
