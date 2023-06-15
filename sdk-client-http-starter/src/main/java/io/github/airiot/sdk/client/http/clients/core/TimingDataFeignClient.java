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

package io.github.airiot.sdk.client.http.clients.core;

import feign.Param;
import feign.RequestLine;
import io.github.airiot.sdk.client.builder.LatestDataQuery;
import io.github.airiot.sdk.client.builder.TimingDataQuery;
import io.github.airiot.sdk.client.service.core.dto.timing.BuiltinTimingDataQueryResult;
import io.github.airiot.sdk.client.service.core.dto.latest.LatestData;
import io.github.airiot.sdk.client.service.core.dto.timing.TimingData;
import io.github.airiot.sdk.client.service.core.dto.timing.TimingDataSeries;
import io.github.airiot.sdk.client.http.feign.JsonParamExpander;
import io.github.airiot.sdk.client.service.core.TimingDataClient;

import java.util.List;

public interface TimingDataFeignClient extends TimingDataClient {

    @RequestLine(value = "GET /core/data/query?query={query}")
    BuiltinTimingDataQueryResult query(@Param("query") String query);

    @RequestLine(value = "GET /core/data/latest?query={query}")
    List<LatestData> queryLatest(@Param("query") String query);

    @Override
    default List<TimingData> query(List<TimingDataQuery> queries) {
        return this.query(JsonParamExpander.INSTANCE.expand(queries)).parse();
    }

    @Override
    default List<LatestData> queryLatest(LatestDataQuery query) {
        return this.queryLatest(JsonParamExpander.INSTANCE.expand(query.getSpecifications()));
    }
}
