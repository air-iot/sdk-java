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

import io.github.airiot.sdk.client.builder.LatestDataQuery;
import io.github.airiot.sdk.client.builder.TimingDataQuery;
import io.github.airiot.sdk.client.http.annotation.GetObjectParams;
import io.github.airiot.sdk.client.service.core.TimingDataClient;
import io.github.airiot.sdk.client.service.core.dto.latest.LatestData;
import io.github.airiot.sdk.client.service.core.dto.timing.TimingData;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import javax.annotation.Nonnull;
import java.util.List;

@HttpExchange
public interface TimingDataClientImpl extends TimingDataClient {

    @GetExchange(value = "/core/data/query")
    List<TimingData> query(@Nonnull @GetObjectParams("query") List<TimingDataQuery> queries);

    @GetExchange(value = "/core/data/latest")
    List<LatestData> queryLatest(@Nonnull @GetObjectParams LatestDataQuery query);

}
