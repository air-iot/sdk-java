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


import com.google.gson.Gson;
import io.github.airiot.sdk.client.builder.LatestDataQuery;
import io.github.airiot.sdk.client.builder.TimingDataQuery;
import io.github.airiot.sdk.client.http.feign.JsonParamExpander;
import io.github.airiot.sdk.client.service.core.TimingDataClient;
import io.github.airiot.sdk.client.service.core.dto.timing.TimingData;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpTimingDataClientTests {

    @Autowired
    private TimingDataClient timingDataClient;

    @Test
    void queryHistory() {
        List<TimingDataQuery> queries = TimingDataQuery.newBuilder()
                .select("b")
                .selectAs("max(\"a\")", "FA")
                .table("tcp_client")
                .timeBetween(LocalDateTime.now().minusDays(14), LocalDateTime.now())
                .groupByNode()
                .groupByTime("1d")
                .fill("-100")
                .finish()
                .select("id", "b")
                .table("tcp_client")
                .timeBetween(LocalDateTime.now().minusDays(14), LocalDateTime.now())
                .finish()
                .build();
        
        System.out.println(JsonParamExpander.INSTANCE.expand(queries));
        List<TimingData> result = this.timingDataClient.query(queries);
        System.out.println(new Gson().toJson(result));
    }

    @Test
    void queryLatest() {
        LatestDataQuery query = LatestDataQuery.create()
                .allTags("tcp_client", "tcp_client_001")
                .specific("tcp_client", "tcp_client_002", "b");
        Object result = this.timingDataClient.queryLatest(query);

        System.out.println(JsonParamExpander.INSTANCE.expand(query.getSpecifications()));
        System.out.println(result);
    }
}
