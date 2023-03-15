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

package com.github.airiot.sdk.client.properties;

public enum ServiceType {

    /**
     * 核心服务
     */
    CORE("core"),

    /**
     * 数据接口服务
     */
    DATA_SERVICE("data-service"),

    /**
     * 告警服务
     */
    WARNING("warning"),

    /**
     * 空间管理服务
     */
    SPM("spm");

    private final String name;

    public String getName() {
        return name;
    }

    ServiceType(String name) {
        this.name = name;
    }

    public static ServiceType of(String serviceName) {
        return ServiceType.valueOf(serviceName.toUpperCase());
    }
}
