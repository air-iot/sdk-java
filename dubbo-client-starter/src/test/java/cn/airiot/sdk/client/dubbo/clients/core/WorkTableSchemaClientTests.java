package cn.airiot.sdk.client.dubbo.clients.core;


import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.context.RequestContext;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.service.core.TableSchemaClient;
import cn.airiot.sdk.client.service.core.dto.table.TableSchema;
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
public class WorkTableSchemaClientTests {

    @Autowired
    private TableSchemaClient tableSchemaClient;

    private final String testTableId = "employee";
    private final String testRowId = "integration_001";

    @BeforeAll
    void init() {
        RequestContext.setProjectId("625f6dbf5433487131f09ff7");
    }

    @AfterAll
    void clear() {

    }

    @Test
    @Order(1)
    void queryById() {
        Response<TableSchema> response = this.tableSchemaClient.queryById("employee");
        
        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到数据");
    }

    @Test
    @Order(2)
    void queryByTitle() {
        Response<List<TableSchema>> response = this.tableSchemaClient.query(Query.newBuilder()
                .select(TableSchema.class)
                .eq(TableSchema::getTitle, "员工信息")
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到数据");
        Assert.isTrue(response.getData().size() == 1, "查询到记录数量不匹配, expected: 1, got: " + response.getData().size());
    }


    @Test
    @Order(3)
    void queryAll() {
        Response<List<TableSchema>> response = this.tableSchemaClient.queryAll();
        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到数据");
    }

}
