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
import io.github.airiot.sdk.client.service.core.RoleClient;
import io.github.airiot.sdk.client.service.core.dto.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpRoleClientTests {

    @Autowired
    private RoleClient roleClient;

    private String createRoleId;

    @AfterAll
    void cleanup() {
        this.roleClient.deleteById(this.createRoleId);
    }

    @Test
    @Order(1)
    void createRole() {
        Role role = new Role();
        role.setName("集成测试角色");
        role.setDescription("这是一个集成测试角色");
        role.setPermission(Arrays.asList("homeDashboardConfig.view", "homeDashboardConfig", "ext_tcpserver.view", "ext_tcpserver.add", "ext_tcpserver.edit", "ext_tcpserver.delete", "ext_tcpserver", "flow.view", "flow.add", "flow.edit", "flow.delete", "flow", "data.view", "data"));

        ResponseDTO<InsertResult> response = this.roleClient.create(role);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "创建角色失败, 未返回角色ID");
        this.createRoleId = response.getData().getInsertedID();
    }

    @Test
    @Order(2)
    void testQueryById() {
        ResponseDTO<Role> response = this.roleClient.queryById(this.createRoleId);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "未查询到角色信息");
    }

    @Test
    @Order(3)
    void testQueryAll() {
        ResponseDTO<List<Role>> response = this.roleClient.queryAll();
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertTrue(response.getData().stream().anyMatch(r -> r.getId().equals(this.createRoleId)), "未查询到角色信息");
    }

    @Test
    @Order(4)
    void testQueryByName() {
        ResponseDTO<List<Role>> response = this.roleClient.queryByName("集成测试角色");
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertTrue(response.getData().stream().anyMatch(r -> r.getId().equals(this.createRoleId)), "未查询到角色信息");
    }

    @Test
    @Order(5)
    void testUpdateRole() {
        Role role = new Role();
        role.setName("集成测试角色-更新");
        role.setDisabled(true);

        ResponseDTO<Void> response = this.roleClient.update(this.createRoleId, role);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());

        ResponseDTO<Role> queryResponse = this.roleClient.queryById(this.createRoleId);
        Assertions.assertTrue(queryResponse.isSuccess(), response.getMessage());
        Assertions.assertNotNull(queryResponse.getData(), "未查询到角色信息");
        Assertions.assertTrue(queryResponse.getData().getDisabled(), "角色未被禁用");
        Assertions.assertEquals("集成测试角色-更新", queryResponse.getData().getName(), "角色名称未修改");
    }
}
