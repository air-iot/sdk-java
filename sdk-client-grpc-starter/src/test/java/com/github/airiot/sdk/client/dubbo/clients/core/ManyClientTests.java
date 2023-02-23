package com.github.airiot.sdk.client.dubbo.clients.core;


import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.context.RequestContext;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dubbo.clients.spm.DubboProjectClient;
import com.github.airiot.sdk.client.service.spm.dto.Project;
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
public class ManyClientTests {


    @Autowired
    private DubboProjectClient projectClient;

    @Autowired
    private DubboTableDataClient tableDataClient;

    @BeforeAll
    void init() {
        RequestContext.setProjectId("625f6dbf5433487131f09ff7");
    }

    @Test
    void queryDepartmentAndTable() {
        Response<List<WorkTableDataClientTests.Employee>> employees = this.tableDataClient.query(WorkTableDataClientTests.Employee.class, "employee", Query.newBuilder()
                .select(WorkTableDataClientTests.Employee.class)
                .build());
        Response<List<Project>> projects = this.projectClient.queryAll();

        Assertions.assertTrue(projects.isSuccess(), projects.getFullMessage());
        Assertions.assertTrue(employees.isSuccess(), employees.getFullMessage());
    }
}
