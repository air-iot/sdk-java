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
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.dto.Role;
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
public class RoleClientTests {

    @Autowired
    private DubboRoleClient roleClient;

    private final String testRoleName = "集成测试角色";

    private String testRoleId = "admin";

    @Test
    @Order(2)
    void getById() {
        ResponseDTO<Role> responseDTO = this.roleClient.queryById(this.testRoleId);
        Assert.isTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assert.notNull(responseDTO.getData(), "未查询到角色信息");

        Role role = responseDTO.getData();
        Assert.isTrue(this.testRoleName.equals(role.getName()), "角色名称不匹配");
    }

    @Test
    @Order(3)
    void query() {
        ResponseDTO<List<Role>> responseDTO = this.roleClient.query(Query.newBuilder()
                .select(Role.class)
                .filter()
                .eq("name", "超级管理员")
                .end()
                .build());

        Assert.isTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assert.notEmpty(responseDTO.getData(), "未查询到角色");
    }
}
