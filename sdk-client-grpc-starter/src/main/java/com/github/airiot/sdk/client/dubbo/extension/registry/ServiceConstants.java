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

package com.github.airiot.sdk.client.dubbo.extension.registry;


/**
 * 常量
 */
public interface ServiceConstants {

    /**
     * 服务实例ID
     * <br>
     * 元数据存储在 {@link org.apache.dubbo.common.URL} 的 attribute 中
     */
    String SERVICE_INSTANCE_ID = "instanceId";

    /**
     * 元数据 Key
     * <br>
     * 元数据存储在 {@link org.apache.dubbo.common.URL} 的 attribute 中
     */
    String METADATA_KEY = "metadata";

    /**
     * 服务实例版本号
     * <br>
     * 元数据存储在 {@link org.apache.dubbo.common.URL} 的 attribute 中
     */
    String VERSION_KEY = "version";
}
