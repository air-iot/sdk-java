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

package io.github.airiot.sdk.client.http.clients;


import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.http.clients.common.Context;
import io.github.airiot.sdk.client.http.clients.common.HttpCommonClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpCommonClientTests {


    @Autowired
    private HttpCommonClient client;

    @Test
    void testGet() {
        ResponseDTO<Map> response = client.get("/spm/admin/check", Map.class);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        Assertions.assertNotNull(response.getData(), "data is null");
        Assertions.assertTrue(response.getData().containsKey("status"));
        System.out.println(response);
    }

    @Test
    void testGetWithContext() {
        ResponseDTO<Token> response = client.get(
                Context.newBuilder().project("647d3f6db395ea47865d4b9e").build(),
                "/core/auth/token?appkey=d541d5db-cb9e-c0c7-14b8-36d5a0011d87&appsecret=e650e169-835e-a451-6459-2bc629200722", Token.class);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        Assertions.assertNotNull(response.getData(), "data is null");
        System.out.println(response);
    }

    @Test
    void testGetWithContextFailed() {
        ResponseDTO<Token> response = client.get(
                Context.newBuilder().build(),
                "/core/auth/token?appkey=d541d5db-cb9e-c0c7-14b8-36d5a0011d87&appsecret=e650e169-835e-a451-6459-2bc629200722", Token.class);
        Assertions.assertFalse(response.isSuccess(), "should be failed");
        System.out.println(response);
    }

    @Test
    void testPost() {
        Map<String, Object> student = new HashMap<>();
        student.put("name", "从通用客户端创建");
        student.put("age", 23);
        student.put("sex", "male");

        ResponseDTO<InsertResult> response = client.post(
                Context.newBuilder().project("647d3f6db395ea47865d4b9e").build(),
                "/core/t/student/d", student, InsertResult.class);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        Assertions.assertNotNull(response.getData(), "data is null");
        Assertions.assertTrue(StringUtils.hasText(response.getData().getInsertedID()), "insertedID is null");
        System.out.println(response);
    }

    @Test
    void testPut() {
        Map<String, Object> student = new HashMap<>();
        student.put("name", "从通用客户端创建(Put)");
        student.put("age", 32);
        student.put("sex", "female");

        ResponseDTO<Void> response = client.put(
                Context.newBuilder().project("647d3f6db395ea47865d4b9e").build(),
                "/core/t/student/d/650d344dbde814942b01c6e7", student, Void.class);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        System.out.println(response);
    }

    @Test
    void testPatch() {
        Map<String, Object> student = new HashMap<>();
        student.put("name", "从通用客户端创建(Patch)");

        ResponseDTO<Void> response = client.patch(
                Context.newBuilder().project("647d3f6db395ea47865d4b9e").build(),
                "/core/t/student/d/650d344dbde814942b01c6e7", student, Void.class);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        System.out.println(response);
    }

    @Test
    void testDelete() {
        ResponseDTO<Void> response = client.delete(
                Context.newBuilder().project("647d3f6db395ea47865d4b9e").build(),
                "/core/t/student/d/650d344dbde814942b01c6e7", null, Void.class);
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        System.out.println(response);
    }
}
