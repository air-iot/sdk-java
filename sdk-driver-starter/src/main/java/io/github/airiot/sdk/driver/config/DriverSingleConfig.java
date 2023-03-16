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
 * 单一驱动配置.
 * <br>
 * 如果驱动实例、模型和设备的配置相同, 或者配置类中包含了驱动实例、模型和设备中配置的全部信息时可使用该类作为驱动配置类.
 *
 * <pre>
 *     // 数据点信息
 *     public class MyTag extends Tag {
 *         // ...
 *     }
 *     // 指令信息
 *     public class MyCommand {
 *         // ...
 *     }
 *     // 全配置类, 该类中的属性包含了驱动实例、模型和设备中定义的全部配置信息
 *     public class FullConfig {
 *         // 驱动实例配置字段
 *         // 模型配置字段
 *         // 设备配置字段
 *     }
 *
 *     // 自定义驱动
 *     public class MyDriver implements DriverApp&lt;DriverSingleConfig&lt;FullConfig&gt;, MyTag, MyCommand&gt; {
 *
 *
 *     }
 *
 * </pre>
 *
 * @param <Config> 配置类泛型
 */
public class DriverSingleConfig<Config> {

    /**
     * 模型和设备均使用父类的配置泛型类型
     */
    public class Model extends io.github.airiot.sdk.driver.config.Model<Config, Config> {

    }

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
    private Config config;
    /**
     * 驱动下模型列表(工作表)
     */
    private List<Model> tables;

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

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public List<Model> getTables() {
        return tables;
    }

    public void setTables(List<Model> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "DriverSingleConfig{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", driverType='" + driverType + '\'' +
                ", config=" + config +
                ", tables=" + tables +
                '}';
    }
}
