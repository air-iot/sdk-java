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

package io.github.airiot.sdk.client.dubbo.clients.spm;


import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.service.spm.SpmUserClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenTests {

    @Autowired
    private EmbedClass embed;


    @Configuration
    public static class EmbedConfiguration {

        @Bean
        public EmbedClass embedClass() {
            return new EmbedClass();
        }
    }

    public static class EmbedClass {

        @Autowired
        private SpmUserClient spmUserClient;

        public ResponseDTO<Token> getToken1() {
            return this.getToken2();
        }

        public ResponseDTO<Token> getToken2() {
            return this.getToken3();
        }

        public ResponseDTO<Token> getToken3() {
            return this.spmUserClient.getToken("9a5890c2-786e-4bcc-9355-f5bf4ceeef93", "5cbe12a9-cf7f-4188-a81b-9510f692166c");
        }
    }

    @Test
    void getTokenEmbed() {
        ResponseDTO<Token> responseDTO = this.embed.getToken1();
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assertions.assertNotNull(responseDTO.getData(), "未查询到 token 信息");
    }
}
