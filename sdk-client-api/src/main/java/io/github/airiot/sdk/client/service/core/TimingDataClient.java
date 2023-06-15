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

package io.github.airiot.sdk.client.service.core;


import io.github.airiot.sdk.client.builder.LatestDataQuery;
import io.github.airiot.sdk.client.builder.TimingDataQuery;
import io.github.airiot.sdk.client.service.PlatformClient;
import io.github.airiot.sdk.client.service.core.dto.latest.LatestData;
import io.github.airiot.sdk.client.service.core.dto.timing.TimingData;

import java.util.List;

/**
 * 时序数据客户端
 */
public interface TimingDataClient extends PlatformClient {

    /**
     * 查询历史数据
     *
     * @param queries 查询信息
     * @return 查询结果
     */
    List<TimingData> query(List<TimingDataQuery> queries);

    /**
     * 查询指定数据点的最新数据. 可同时查询多个设备的数据点最新数据.
     *
     * @param query 要查询的数据点列表
     * @return 数据点的最新数据
     */
    List<LatestData> queryLatest(LatestDataQuery query);

}
