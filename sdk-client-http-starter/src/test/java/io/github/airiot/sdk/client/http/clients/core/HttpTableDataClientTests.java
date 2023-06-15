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


import io.github.airiot.sdk.client.annotation.WorkTable;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.BatchInsertResult;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.UpdateOrDeleteResult;
import io.github.airiot.sdk.client.service.core.SpecificTableDataClient;
import io.github.airiot.sdk.client.service.core.TableDataClientFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpTableDataClientTests {

    @Autowired
    private TableDataClientFactory tableDataClientFactory;

    private SpecificTableDataClient<TableData> tableDataClient;

    private String createId;

    private final List<String> insertIds = new ArrayList<>();

    @BeforeAll
    void setup() {
        this.tableDataClient = tableDataClientFactory.newClient(TableData.class);
    }

    @AfterAll
    void cleanup() {
        ResponseDTO<Void> response1 = this.tableDataClient.deleteById(this.createId);
        Assertions.assertTrue(response1.isSuccess(), response1.getMessage());

        ResponseDTO<UpdateOrDeleteResult> response2 = this.tableDataClient.deleteByQuery(Query.newBuilder()
                .filter().in(TableData::getId, this.insertIds).end().build());
        Assertions.assertTrue(response2.isSuccess(), response2.getMessage());
        Assertions.assertNotNull(response2.getData(), "未返回数据");
        Assertions.assertEquals(2, response2.getData().getCount(), "删除记录数量不正确");
    }

    @Test
    @Order(1)
    void create() {
        TableData row = new TableData();
        row.setName("张三");
        row.setAge(18);
        row.setGender("male");
        row.setBirthday("2001-02-03");

        ResponseDTO<InsertResult> response = this.tableDataClient.create(row);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "无返回结果");
        Assertions.assertNotNull(response.getData().getInsertedID(), "未返回新增记录ID");

        this.createId = response.getData().getInsertedID();
    }

    @Test
    @Order(2)
    void createBatch() {
        TableData row1 = new TableData();
        row1.setName("李四");
        row1.setAge(20);
        row1.setGender("male");
        row1.setBirthday("2002-03-04");

        TableData row2 = new TableData();
        row2.setName("小红");
        row2.setAge(19);
        row2.setGender("female");
        row2.setBirthday("2000-01-02");

        ResponseDTO<BatchInsertResult> response = this.tableDataClient.create(Arrays.asList(row1, row2));
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "无返回结果");
        Assertions.assertNotNull(response.getData().getInsertedIDs(), "未返回新增记录ID");

        insertIds.addAll(response.getData().getInsertedIDs());
    }

    @Test
    @Order(3)
    void queryById() {
        ResponseDTO<TableData> response = this.tableDataClient.queryById(this.createId);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "无返回结果");
        Assertions.assertEquals(response.getData().getName(), "张三", "name不匹配");
        Assertions.assertEquals(response.getData().getAge(), 18, "age不匹配");
        Assertions.assertEquals(response.getData().getGender(), "male", "gender不匹配");
        Assertions.assertEquals(response.getData().getBirthday(), "2001-02-03T00:00:00+08:00", "birthday不匹配");
    }

    @Test
    @Order(4)
    void queryByName() {
        ResponseDTO<List<TableData>> response = this.tableDataClient.query(Query.newBuilder()
                .select(TableData.class)
                .filter()
                .eq(TableData::getName, "张三")
                .end()
                .build());
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "无返回结果");
        Assertions.assertTrue(response.getData().stream().anyMatch(data -> data.getName().equals("张三")), "name不匹配");
    }
    
    @Test
    @Order(5)
    void queryPage() {
        ResponseDTO<List<TableData>> response = this.tableDataClient.query(Query.newBuilder()
                .select(TableData.class)
                .withCount()
                .skip(1)
                .limit(2)
                .build());
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "无返回结果");
        Assertions.assertEquals(2, response.getData().size(), "数量不匹配");
    }

    @Test
    @Order(6)
    void batchUpdate() {
        TableData data = new TableData();
        data.setAge(23);
        ResponseDTO<UpdateOrDeleteResult> response = this.tableDataClient.update(Query.newBuilder()
                .filter()
                .lte(TableData::getAge, 19)
                .gte(TableData::getAge, 18)
                .end()
                .build(), data);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "无返回结果");
        Assertions.assertEquals(2, response.getData().getCount(), "更新行数不正确");
    }

    @Test
    @Order(7)
    void replace() {
        TableData data = new TableData();
        data.setId(this.createId);
        data.setName("张三-replace");
        data.setAge(28);
        data.setGender("female");
        data.setBirthday("2021-12-23");

        ResponseDTO<Void> response = this.tableDataClient.replace(this.createId, data);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());

        ResponseDTO<TableData> response1 = this.tableDataClient.queryById(this.createId);
        Assertions.assertTrue(response1.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response1.getData(), "无返回结果");
        Assertions.assertEquals(response1.getData().getName(), data.getName(), "name不匹配");
        Assertions.assertEquals(response1.getData().getAge(), data.getAge(), "age不匹配");
        Assertions.assertEquals(response1.getData().getGender(), data.getGender(), "gender不匹配");
        Assertions.assertEquals(response1.getData().getBirthday(), "2021-12-23T00:00:00+08:00", "birthday不匹配");
    }

    @WorkTable("integration_test")
    static class TableData {
        private String id;
        private String name;
        private int age;
        private String gender;
        private String birthday;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }
    }
}
