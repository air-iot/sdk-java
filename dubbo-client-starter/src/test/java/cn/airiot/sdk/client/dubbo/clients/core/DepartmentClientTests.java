package cn.airiot.sdk.client.dubbo.clients.core;


import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.service.core.dto.Department;
import cn.airiot.sdk.client.dto.Response;
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
        Response<Department> response = this.departmentClient.queryById("department_integration_test");
        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到部门");

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到部门");
    }

    @Test
    @Order(3)
    void query() {
        Response<List<Department>> response = this.departmentClient.query(Query.newBuilder()
                .select(Department.class)
                .eq("name", "集成测试部门")
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notEmpty(response.getData(), "未查询到部门");
    }
}
