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

package com.github.airiot.sdk.client.dubbo.clients.core;


import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.context.RequestContext;
import com.github.airiot.sdk.client.dto.InsertResult;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkTableDataClientTests {

    @Autowired
    private DubboTableDataClient tableDataClient;


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
    void create() {
        Employee employee = new Employee();
        employee.setId(testRowId);
        employee.setName("员工001");
        employee.setAge(31);

        Response<InsertResult> response = this.tableDataClient.create(testTableId, employee);

        Assert.isTrue(response.isSuccess(), DubboClientUtils.format(response));
        Assert.notNull(response.getData(), "请求成功, 但返回数据为 null");
        Assert.hasText(response.getData().getInsertedID(), "新增记录ID为空");
    }

    @Test
    @Order(2)
    void getById() {
        Response<Employee> employee = this.tableDataClient.queryById(Employee.class, testTableId, testRowId);
        Assert.isTrue(employee.isSuccess(), employee.getFullMessage());
        Assert.notNull(employee.getData(), "未查询到数据");
        Assert.isTrue(this.testRowId.equals(employee.getData().id), "返回数据的 rowId 与 " + this.testRowId + " 不匹配");
    }

    @Test
    @Order(3)
    void queryById() {
        Response<List<Employee>> response = this.tableDataClient.query(Employee.class, testTableId, Query.newBuilder()
                .select("id", "name", "age")
                .eq("id", this.testRowId)
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到数据");
        Assert.isTrue(response.getData().size() == 1, "查询到记录数量不匹配, expected: 1, got: " + response.getData().size());
        Assert.isTrue(this.testRowId.equals(response.getData().get(0).id), "返回数据的 rowId 与 " + this.testRowId + " 不匹配");
    }

    @Test
    @Order(4)
    void queryByIn() {
        Response<List<Employee>> response = this.tableDataClient.query(Employee.class, testTableId, Query.newBuilder()
                .select("id", "name", "age")
                .in("id", this.testRowId)
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notNull(response.getData(), "未查询到数据");
        Assert.isTrue(response.getData().size() == 1, "查询到记录数量不匹配, expected: 1, got: " + response.getData().size());
        Assert.isTrue(this.testRowId.equals(response.getData().get(0).id), "返回数据的 rowId 与 " + this.testRowId + " 不匹配");
    }

    @Test
    @Order(5)
    void queryAll() {
        Response<List<Employee>> response = this.tableDataClient.query(Employee.class, testTableId, Query.newBuilder()
                .select("id", "name", "age")
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notEmpty(response.getData(), "未查询到数据");
    }

    @Test
    @Order(6)
    void queryNotEquals() {
        Response<List<Employee>> response = this.tableDataClient.query(Employee.class, testTableId, Query.newBuilder()
                .select("id", "name", "age")
                .ne("id", testRowId)
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());
        Assert.notEmpty(response.getData(), "未查询到数据");
    }

    @Test
    @Order(7)
    void queryBetween() {
        int minValue = 0;
        int maxValue = 32;
        Response<List<Employee>> response = this.tableDataClient.query(Employee.class, testTableId, Query.newBuilder()
                .select("id", "name", "age")
                .between("age", minValue, maxValue)
                .build());

        Assert.isTrue(response.isSuccess(), response.getFullMessage());

        List<Employee> employees = response.getData();
        if (employees == null) {
            return;
        }

        for (Employee employee : employees) {
            Integer age = employee.getAge();
            if (age == null || age < minValue || age >= maxValue) {
                throw new IllegalStateException("返回结果不正确, 返回结果中存在年龄不在 [" + minValue + ", " + maxValue + ") 范围内的数据, " + employee);
            }
        }
    }

    @Test
    @Order(8)
    void countAge() {
        Response<List<Map<String, Object>>> response = this.tableDataClient.query(testTableId, Query.newBuilder()
                .select("id", "name")
                .summary(Employee::getAge).count("countAge")
                .summary(Employee::getAge).sum("sumAge")
                .summary(Employee::getAge).min("minAge")
                .summary(Employee::getAge).max("maxAge")
                .groupBy(Employee::getName).sameToField()
                .build());
        System.out.println(response);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
    }

    @Test
    @Order(9)
    void replace() {
        Employee employee = this.tableDataClient.queryById(Employee.class, this.testTableId, this.testRowId).unwrap();

        employee.setName("replaced-" + employee.name);
        employee.setAge(employee.getAge() + 100);

        Response<Void> response = this.tableDataClient.replace(this.testTableId, this.testRowId, employee);
        Assert.isTrue(response.isSuccess(), response.getFullMessage());

        Employee employeeNew = this.tableDataClient.queryById(Employee.class, this.testTableId, this.testRowId).unwrap();
        Assert.isTrue(employeeNew.getName().equals(employee.getName()), "替换后的名称不匹配");
        Assert.isTrue(employeeNew.getAge().equals(employee.getAge()), "替换后的年龄不匹配");
    }

    @Test
    @Order(10)
    void deleteById() {
        Response<Void> response = this.tableDataClient.deleteById(testTableId, testRowId);
        Assert.isTrue(response.isSuccess(), response.getFullMessage());
    }

    public static class Employee {
        private String id;
        private String name;
        private Integer age;

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

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

}
