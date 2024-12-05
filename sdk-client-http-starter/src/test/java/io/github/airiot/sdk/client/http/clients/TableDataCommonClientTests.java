package io.github.airiot.sdk.client.http.clients;

import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.BatchInsertResult;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.UpdateOrDeleteResult;
import io.github.airiot.sdk.client.http.clients.core.TableDataCommonClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TableDataCommonClientTests {

    @Autowired
    private TableDataCommonClient client;
    
    @Test
    void testCreateRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("name", "小明");
        row.put("age", 18);
        row.put("sex", "male");
        row.put("birthday", LocalDate.now());
        row.put("classTime", LocalTime.now());
        row.put("fjDateTime", LocalDateTime.now());
        ResponseDTO<InsertResult> response = this.client.create("647d3f6db395ea47865d4b9e", "student", row);
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isSuccess());
        Assertions.assertNotNull(response.getData());
        Assert.hasText(response.getData().getInsertedID(), "新增记录ID为空");
        System.out.println(response.getData().getInsertedID());
    }

    @Test
    void testCreateRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("name", "小明");
        row1.put("age", 18);
        row1.put("sex", "male");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("name", "小红");
        row2.put("age", 17);
        row2.put("sex", "female");

        rows.add(row1);
        rows.add(row2);

        ResponseDTO<BatchInsertResult> response = this.client.create("647d3f6db395ea47865d4b9e", "student", rows);
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        Assertions.assertNotNull(response.getData(), response.getFullMessage());
        Assertions.assertNotNull(response.getData().getInsertedIDs(), response.getFullMessage());
        System.out.println(response.getData().getInsertedIDs());
    }

    @Test
    void testUpdateRowById() {
        Map<String, Object> row = new HashMap<>();
        row.put("name", "小明1");
        row.put("age", 28);
        row.put("sex", "female");
        ResponseDTO<UpdateOrDeleteResult> response = this.client.update("647d3f6db395ea47865d4b9e", "student", "674558d138b1bae67a93f5a0", row);
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
    }

    @Test
    void testDeleteRowById() {
        ResponseDTO<UpdateOrDeleteResult> response = this.client.deleteById("647d3f6db395ea47865d4b9e", "student", "674558d138b1bae67a93f5a0");
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
    }

    @Test
    void testDeleteRows() {
        Query query = Query.newBuilder()
                .filter()
                .eq("age", 17).end()
                .build();
        ResponseDTO<UpdateOrDeleteResult> response = this.client.deleteByQuery("647d3f6db395ea47865d4b9e", "student", query);
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
    }
}
