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

package io.github.airiot.sdk.client.dubbo.extension.registry;

import java.util.List;
import java.util.Map;


/**
 * 服务实例信息
 */
public class ServiceInstance {

    /**
     * 服务实例ID
     */
    private String id;
    /**
     * 服务名称
     */
    private String name;
    /**
     * 服务实例版本号
     */
    private String version;
    /**
     * 元数据
     */
    private Map<String, String> metadata;
    /**
     * 服务实例 Endpoints
     */
    private List<String> endpoints;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", metadata=" + metadata +
                ", endpoints=" + endpoints +
                '}';
    }
}
