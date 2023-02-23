package com.github.airiot.sdk.client.dubbo.clients.core;


import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.context.RequestContext;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.service.core.dto.User;
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
public class UserClientTests {

    @Autowired
    private DubboUserClient userClient;

    private final String testUserName = "chenpc_integration";

    private String testUserId = "chenpc_integration";

    @BeforeAll
    void init() {
        RequestContext.setProjectId("625f6dbf5433487131f09ff7");
    }

    @AfterAll
    void clear() {
        this.userClient.deleteById("chenpc_integration");
    }

    @Test
    @Order(1)
    void createUser() {
        User user = new User();
        user.setId(testUserId);
        user.setName("chenpc_integration");
        user.setPassword("dell123");
        user.setPhone("189xxxx1536");

        Response<User> response = this.userClient.create(user);

        Assert.isTrue(response.isSuccess(), response.getFullMessage());

        this.testUserId = response.getData().getId();
    }

    @Test
    @Order(2)
    void getUserById() {
        Response<User> response = this.userClient.getById(this.testUserId);
        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到用户信息");

        User user = response.getData();
        Assert.isTrue("189xxxx1536".equals(user.getPhone()), "用户手机号不区配");
    }

    @Test
    @Order(3)
    void queryUser() {
        Response<List<User>> response = this.userClient.query(Query.newBuilder()
                .select("name", "phone", "password")
                .eq(User::getId, this.testUserId)
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notEmpty(response.getData(), "未查询到用户");
    }

    @Test
    @Order(3)
    void queryUserIn() {
        Response<List<User>> response = this.userClient.query(Query.newBuilder()
                .select("name", "phone", "password")
                .in("id", this.testUserId)
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notEmpty(response.getData(), "未查询到用户");
    }

    @Test
    @Order(3)
    void queryUserNotIn() {
        Response<List<User>> response = this.userClient.query(Query.newBuilder()
                .select("name", "phone", "password")
                .notIn("id", this.testUserId)
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notEmpty(response.getData(), "未查询到用户");
    }

    @Test
    @Order(4)
    void updateUser() {
        User user = new User();
        user.setId(testUserId);
        user.setPhone("189****1536");

        Response<Void> response = this.userClient.update(user);
        Assert.isTrue(response.isSuccess(), response.getFullMessage());

        Response<User> queryUser = this.userClient.getById(testUserId);
        Assert.isTrue(queryUser.isSuccess(), DubboClientUtils.format(response));
    }
}
