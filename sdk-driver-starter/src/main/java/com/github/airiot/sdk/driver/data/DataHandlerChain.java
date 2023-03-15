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

package com.github.airiot.sdk.driver.data;

import com.github.airiot.sdk.driver.model.Point;
import com.github.airiot.sdk.driver.model.Tag;

/**
 * 采集数据处理链
 */
public interface DataHandlerChain {

    /**
     * 依次执行所有的处理器对采集到的数据进行处理, 并返回最终处理结果
     *
     * @param tableId  设备所属表标识
     * @param deviceId 设备标识
     * @param tag      数据点信息
     * @param value    采集到的数据
     * @return 处理后的结果数据. 如果返回结果为 {@code null} 则表示丢弃该数据点的数据
     */
    <T extends Tag> Object handle(String tableId, String deviceId, T tag, Object value);

    /**
     * 依次执行所有的处理器对一个资产下所有采集到的数据进行处理, 并返回最终处理结果
     *
     * @param point 资产采集到的数据
     * @return 处理后的结果数据
     */
    Point handle(Point point);

}
