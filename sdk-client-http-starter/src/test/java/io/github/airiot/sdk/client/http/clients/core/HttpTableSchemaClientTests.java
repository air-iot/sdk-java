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


import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.TableSchemaClient;
import io.github.airiot.sdk.client.service.core.dto.table.TableSchema;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpTableSchemaClientTests {

    private final String tableId = "integration_test";

    @Autowired
    private TableSchemaClient tableSchemaClient;
    
    @Test
    @Order(1)
    void queryById() {
        ResponseDTO<TableSchema> response = tableSchemaClient.queryById(tableId);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "未查询工作表信息");
    }

    @Test
    @Order(2)
    void queryAll() {
        ResponseDTO<List<TableSchema>> response = tableSchemaClient.queryAll();
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "未查询工作表信息");
        Assertions.assertTrue(response.getData().stream().anyMatch(t -> t.getId().equals(this.tableId)), "未查询工作表信息");
    }

    @Test
    @Order(3)
    void query() {
        ResponseDTO<List<TableSchema>> response = tableSchemaClient.query(Query.newBuilder()
                .select(TableSchema.class)
                .filter()
                .eq(TableSchema::getTitle, "集成测试")
                .end()
                .build());
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "未查询工作表信息");
        Assertions.assertTrue(response.getData().stream().anyMatch(t -> t.getId().equals(this.tableId)), "未查询工作表信息");
    }
}
