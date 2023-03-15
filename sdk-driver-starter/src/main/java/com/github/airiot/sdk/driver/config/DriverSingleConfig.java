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
public class DriverSingleConfig<Config> extends DriverConfig<Config, Config, Config> {

}
