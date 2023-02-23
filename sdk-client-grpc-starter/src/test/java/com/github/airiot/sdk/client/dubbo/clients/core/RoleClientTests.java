package com.github.airiot.sdk.client.dubbo.clients.core;


import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.service.core.dto.Role;
import com.github.airiot.sdk.client.dto.Response;
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
        Response<Role> response = this.roleClient.queryById(this.testRoleId);
        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到角色信息");

        Role role = response.getData();
        Assert.isTrue(this.testRoleName.equals(role.getName()), "角色名称不匹配");
    }
    
    @Test
    @Order(3)
    void query() {
        Response<List<Role>> response = this.roleClient.query(Query.newBuilder()
                .select(Role.class)
                .eq("name", "超级管理员")
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notEmpty(response.getData(), "未查询到角色");
    }
}
