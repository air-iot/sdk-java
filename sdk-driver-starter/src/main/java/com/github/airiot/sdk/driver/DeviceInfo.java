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

package com.github.airiot.sdk.driver;


import com.github.airiot.sdk.driver.model.Tag;

import java.util.Collections;
import java.util.Map;

/**
 * 设备基础信息
 */
public class DeviceInfo<T extends Tag> {

    /**
     * 设备ID
     */
    private final String id;
    /**
     * 设备所属表ID
     */
    private final String tableId;
    /**
     * 设备所属驱动实例ID
     */
    private final String driverInstanceId;
    /**
     * 设备的数据点信息
     * <br>
     * 如果驱动实例、模型上配置了数据点时则会合并进来.
     * <br>
     * <b>注: 如果驱动实例、模型或设备中配置的数据点标识相同时只会保留一份, 并且按照 “设备 &gt; 模型 &gt; 驱动实例” 的优先级进行覆盖 </b>
     */
    private final Map<String, T> tags;

    public String getId() {
        return id;
    }

    public String getTableId() {
        return tableId;
    }

    public String getDriverInstanceId() {
        return driverInstanceId;
    }

    public Map<String, T> getTags() {
        return tags;
    }

    public DeviceInfo(String id, String tableId, String driverInstanceId) {
        this.id = id;
        this.tableId = tableId;
        this.driverInstanceId = driverInstanceId;
        this.tags = Collections.emptyMap();
    }

    public DeviceInfo(String id, String tableId, String driverInstanceId, Map<String, T> tags) {
        this.id = id;
        this.tableId = tableId;
        this.driverInstanceId = driverInstanceId;
        this.tags = tags == null ? Collections.emptyMap() : tags;
    }
}
