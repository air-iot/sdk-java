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
import io.github.airiot.sdk.client.service.core.dto.Department;
import io.github.airiot.sdk.client.dto.ResponseDTO;
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
public class DepartmentClientTests {

    @Autowired
    private DubboDepartmentClient departmentClient;

    @Test
    @Order(2)
    void getById() {
        ResponseDTO<Department> responseDTO = this.departmentClient.queryById("department_integration_test");
        Assert.isTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assert.notNull(responseDTO.getData(), "未查询到部门");

        Assert.isTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assert.notNull(responseDTO.getData(), "未查询到部门");
    }

    @Test
    @Order(3)
    void query() {
        ResponseDTO<List<Department>> responseDTO = this.departmentClient.query(Query.newBuilder()
                .select(Department.class)
                .eq("name", "集成测试部门")
                .build());

        Assert.isTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assert.notEmpty(responseDTO.getData(), "未查询到部门");
    }
}
