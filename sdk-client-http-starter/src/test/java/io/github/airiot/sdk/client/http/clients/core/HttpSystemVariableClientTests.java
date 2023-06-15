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


import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.SystemVariableClient;
import io.github.airiot.sdk.client.service.core.dto.SystemVariable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpSystemVariableClientTests {

    @Autowired
    private SystemVariableClient systemVariableClient;

    private String createId;

    @AfterAll
    void cleanup() {
        this.systemVariableClient.deleteById(this.createId);
    }

    @Test
    @Order(1)
    void createSystemVariable() {
        SystemVariable variable = new SystemVariable();
        variable.setName("集成测试变量");
        variable.setType(SystemVariable.NUMBER);
        variable.setUid("integration_test_variable");
        variable.setValue(123.456);

        ResponseDTO<InsertResult> response = this.systemVariableClient.create(variable);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "返回值为空");
        Assertions.assertNotNull(response.getData().getInsertedID(), "系统变量ID为空");
        this.createId = response.getData().getInsertedID();
    }

    @Test
    @Order(2)
    void queryById() {
        ResponseDTO<SystemVariable> response = this.systemVariableClient.queryById(this.createId);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "返回值为空");
        Assertions.assertEquals(response.getData().getUid(), "integration_test_variable", "系统变量UID不匹配");
        Assertions.assertEquals(response.getData().getValue(), 123.456, "系统变量值不匹配");
    }

    @Test
    @Order(3)
    void queryByName() {
        ResponseDTO<List<SystemVariable>> response = this.systemVariableClient.queryByName("集成测试变量");
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "返回值为空");
        Assertions.assertFalse(response.getData().isEmpty(), "未查询到系统变量");
        Assertions.assertTrue(response.getData().stream().anyMatch(v -> v.getId().equals(this.createId)), "未找到测试创建的系统变量");
    }

    @Test
    @Order(4)
    void queryAll() {
        ResponseDTO<List<SystemVariable>> response = this.systemVariableClient.queryAll();
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "返回值为空");
        Assertions.assertFalse(response.getData().isEmpty(), "未查询到系统变量");
        Assertions.assertTrue(response.getData().stream().anyMatch(v -> v.getId().equals(this.createId)), "未找到测试创建的系统变量");
    }

    @Test
    @Order(5)
    void queryByUid() {
        ResponseDTO<SystemVariable> response = this.systemVariableClient.queryByUId("integration_test_variable");
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "返回值为空");
        Assertions.assertEquals(response.getData().getUid(), "integration_test_variable", "系统变量UID不匹配");
        Assertions.assertEquals(response.getData().getValue(), 123.456, "系统变量值不匹配");
    }

    @Test
    @Order(6)
    void updateValue() {
        ResponseDTO<Void> response = this.systemVariableClient.updateValue(this.createId, 654.321);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());

        ResponseDTO<SystemVariable> updateResponse = this.systemVariableClient.queryById(this.createId);
        Assertions.assertTrue(updateResponse.isSuccess(), response.getMessage());
        Assertions.assertNotNull(updateResponse.getData(), "返回值为空");
        Assertions.assertEquals(updateResponse.getData().getValue(), 654.321, "系统变量值不匹配");
    }

    @Test
    @Order(7)
    void update() {
        SystemVariable variable = new SystemVariable();
        variable.setId(this.createId);
        variable.setName("集成测试变量-更新");
        variable.setType(SystemVariable.OBJECT);
        variable.setValue(Collections.singletonMap("hello", "world"));

        ResponseDTO<Void> response = this.systemVariableClient.update(this.createId, variable);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());

        ResponseDTO<SystemVariable> updateResponse = this.systemVariableClient.queryById(this.createId);
        Assertions.assertTrue(updateResponse.isSuccess(), response.getMessage());
        Assertions.assertNotNull(updateResponse.getData(), "返回值为空");
        Assertions.assertEquals(updateResponse.getData().getName(), "集成测试变量-更新", "系统变量值不匹配");
        Assertions.assertEquals(updateResponse.getData().getType(), SystemVariable.OBJECT, "系统变量值不匹配");
        Assertions.assertEquals(updateResponse.getData().getValue(), Collections.singletonMap("hello", "world"), "系统变量值不匹配");
    }

    @Test
    @Order(8)
    void replace() {
        SystemVariable variable = new SystemVariable();
        variable.setId(this.createId);
        variable.setUid("integration_test_variable_replaced");
        variable.setName("集成测试变量-替换");
        variable.setType(SystemVariable.OBJECT);
        variable.setValue(Collections.singletonMap("hello", "world"));

        ResponseDTO<Void> response = this.systemVariableClient.update(this.createId, variable);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());

        ResponseDTO<SystemVariable> updateResponse = this.systemVariableClient.queryById(this.createId);
        Assertions.assertTrue(updateResponse.isSuccess(), response.getMessage());
        Assertions.assertNotNull(updateResponse.getData(), "返回值为空");
        Assertions.assertEquals(updateResponse.getData().getUid(), "integration_test_variable_replaced", "系统变量uid不匹配");
        Assertions.assertEquals(updateResponse.getData().getName(), "集成测试变量-替换", "系统变量name不匹配");
        Assertions.assertEquals(updateResponse.getData().getType(), SystemVariable.OBJECT, "系统变量type不匹配");
        Assertions.assertEquals(updateResponse.getData().getValue(), Collections.singletonMap("hello", "world"), "系统变量值不匹配");
    }

}
