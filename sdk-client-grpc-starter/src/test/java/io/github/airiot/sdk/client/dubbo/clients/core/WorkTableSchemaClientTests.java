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

package io.github.airiot.sdk.client.dubbo.clients.core;


import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.context.RequestContext;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.TableSchemaClient;
import io.github.airiot.sdk.client.service.core.dto.table.TableSchema;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkTableSchemaClientTests {

    @Autowired
    private TableSchemaClient tableSchemaClient;

    private final String testTableId = "employee";
    private final String testRowId = "integration_001";

    @BeforeAll
    void init() {
        RequestContext.setProjectId("625f6dbf5433487131f09ff7");
    }

    @AfterAll
    void clear() {

    }

    @Test
    @Order(1)
    void queryById() {
        ResponseDTO<TableSchema> responseDTO = this.tableSchemaClient.queryById("employee");
        
        Assert.isTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assert.notNull(responseDTO.getData(), "未查询到数据");
    }

    @Test
    @Order(2)
    void queryByTitle() {
        ResponseDTO<List<TableSchema>> responseDTO = this.tableSchemaClient.query(Query.newBuilder()
                .select(TableSchema.class)
                .eq(TableSchema::getTitle, "员工信息")
                .build());

        Assert.isTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assert.notNull(responseDTO.getData(), "未查询到数据");
        Assert.isTrue(responseDTO.getData().size() == 1, "查询到记录数量不匹配, expected: 1, got: " + responseDTO.getData().size());
    }


    @Test
    @Order(3)
    void queryAll() {
        ResponseDTO<List<TableSchema>> responseDTO = this.tableSchemaClient.queryAll();
        Assert.isTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assert.notNull(responseDTO.getData(), "未查询到数据");
    }

}
