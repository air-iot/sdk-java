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

package com.github.airiot.sdk.driver.config;

import com.google.gson.annotations.SerializedName;


/**
 * 设备信息
 *
 * @param <Config> 设备配置类泛型
 */
public class Device<Config> {
    /**
     * 设备ID
     */
    private String id;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备所属驱动实例ID
     */
    private String driverInstanceId;
    /**
     * 设备所属表标识(模型ID)
     */
    private String table;
    /**
     * 设备配置信息
     */
    @SerializedName("device")
    private Config config;

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

    public String getDriverInstanceId() {
        return driverInstanceId;
    }

    public void setDriverInstanceId(String driverInstanceId) {
        this.driverInstanceId = driverInstanceId;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", driverInstanceId='" + driverInstanceId + '\'' +
                ", table='" + table + '\'' +
                ", config=" + config +
                '}';
    }
}
