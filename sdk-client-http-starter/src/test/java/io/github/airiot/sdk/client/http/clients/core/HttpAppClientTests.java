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


import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.service.core.AppClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration-v4")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpAppClientTests {

    @Autowired
    private AppClient appClient;

    @Test
    void getToken() {
        ResponseDTO<Token> response = this.appClient.getToken("", "");
    }

    @Test
    void getUserAuth() {
        ResponseDTO<Map<String, Object>> response = this.appClient.getUserAuth("Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDU4MDk3ODYsImlhdCI6MTc0NDYwMDE4NiwibmJmIjoxNzQ0NjAwMTg2LCJzdWIiOiJhZG1pbiIsInByb2plY3RJZCI6IjY3N2NkMGJlYmE1MjcwZTA5MjFmMzg2ZiIsImN1c3RvbSI6eyJ0b2tlblR5cGUiOiJwcm9qZWN0In19.6M7rQ7-a68z_v-LBDrAUgu31Kb69nbWK4xlVay0_78yq3rSuInhlWjg3mBymmC7524JzfElKYe3cz_64WYdwOw", "677cd0beba5270e0921f386f");
        Assertions.assertTrue(response.isSuccess(), response.getFullMessage());
        System.out.println(response.getData());
    }
}
