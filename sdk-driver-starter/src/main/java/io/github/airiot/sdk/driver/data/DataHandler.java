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

package io.github.airiot.sdk.driver.data;

import io.github.airiot.sdk.driver.model.Tag;
import org.springframework.core.Ordered;


/**
 * 数据处理器, 对采集到的数据进行处理
 * <br>
 * 系统中存在多个 {@link DataHandler} 时, 会按 {@link DataHandler#getOrder()} 从小到大进行排序, 并依次执行.
 * 即 {@code order} 值越小优先级越高
 */
public interface DataHandler extends Ordered {

    /**
     * 判断当前处理器是否支持目标 {@link Tag} 或 {@code value}.
     * <br>
     * 默认处理所有值不为 {@code null} 的数据
     *
     * @param tableId  设备所属表标识
     * @param deviceId 设备ID
     * @param tag      数据点信息
     * @param value    采集到的数据
     * @return 如果返回值为 {@code true} 则调用 {@link #handle(String, String, Tag, Object)} 对采集到的数据进行处理, 否则跳过当前处理器
     */
    default <T extends Tag> boolean supports(String tableId, String deviceId, T tag, Object value) {
        return value != null && tag != null;
    }

    /**
     * 对采集到的数据进行处理, 并返回处理后的结果
     *
     * @param tableId  设备所属表标识
     * @param deviceId 设备ID
     * @param tag      数据点信息
     * @param value    采集到的数据
     * @return 处理后的结果数据. 如果返回结果为 {@code null} 则表示丢弃该数据点的数据, 即不上报给平台.
     */
    <T extends Tag> Object handle(String tableId, String deviceId, T tag, Object value);

}
