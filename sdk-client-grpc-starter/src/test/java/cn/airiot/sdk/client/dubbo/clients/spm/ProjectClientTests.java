package cn.airiot.sdk.client.dubbo.clients.spm;


import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.service.spm.dto.Project;
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
public class ProjectClientTests {

    @Autowired
    private DubboProjectClient projectClient;

    @Test
    void queryAll() {
        Response<List<Project>> response = this.projectClient.queryAll();
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
    }

    @Test
    void queryByName() {
        Response<List<Project>> response = this.projectClient.query(Query.newBuilder()
                .eq(Project::getName, "zq测试")
                .build());

        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        Assertions.assertNotNull(response.getData(), "未查询到项目");
        Assertions.assertFalse(response.getData().isEmpty(), "未查询到项目");
    }

    @Test
    void createProject() {
        
    }
}
