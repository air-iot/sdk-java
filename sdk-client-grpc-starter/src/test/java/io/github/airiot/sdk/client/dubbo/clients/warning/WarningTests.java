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

package io.github.airiot.sdk.client.dubbo.clients.warning;

import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.context.RequestContext;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.warning.dto.Warning;
import org.junit.jupiter.api.*;
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
public class WarningTests {

    @Autowired
    private DubboWarnClient warnClient;

    @BeforeAll
    void init() {
        RequestContext.setProjectId("625f6dbf5433487131f09ff7");
    }

    @Test
    @Order(1)
    void createWaring() {
        Warning warning = new Warning();
        warning.setUid("opcua161");
        warning.setLevel("高");
        warning.setType("1d345be4-2567-4764-7890-3ghj278vb342");
        warning.setDesc("这是一段报警描述信息");
        warning.setRemark("这是一段备注信息");

        warning.setTime(LocalDateTime.now());

        warning.setTableId("opcua");
        warning.setRuleid("63fc4ea4af1652c71d68f88f");

        ResponseDTO<InsertResult> responseDTO = this.warnClient.create(warning);
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assertions.assertNotNull(responseDTO.getData(), "创建报警失败");
    }

    @Test
    @Order(1)
    void queryById() {
        ResponseDTO<Warning> responseDTO = this.warnClient.queryById("63fef49f09802fb164d52ed7", "false");

        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assertions.assertNotNull(responseDTO.getData(), "未查询到报警信息");
    }

    @Test
    @Order(2)
    void queryAll() {
        ResponseDTO<List<Warning>> responseDTO = this.warnClient.query(Query.newBuilder()
                .select(Warning.class)
                .build(), "false");

        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assertions.assertNotNull(responseDTO.getData(), "未查询到报警信息");
        Assertions.assertFalse(responseDTO.getData().isEmpty(), "未查询到报警信息");
    }
}
