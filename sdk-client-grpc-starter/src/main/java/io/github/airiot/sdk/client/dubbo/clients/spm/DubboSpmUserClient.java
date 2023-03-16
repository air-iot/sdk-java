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
import io.github.airiot.sdk.client.dubbo.grpc.api.Response;
import io.github.airiot.sdk.client.dubbo.grpc.api.TokenRequest;
import io.github.airiot.sdk.client.dubbo.grpc.spm.DubboUserServiceGrpc;
import io.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import io.github.airiot.sdk.client.service.spm.SpmUserClient;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;


public class DubboSpmUserClient implements SpmUserClient {

    private final DubboUserServiceGrpc.IUserService userService;

    public DubboSpmUserClient(DubboUserServiceGrpc.IUserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseDTO<Token> getToken(@Nonnull String appKey, @Nonnull String appSecret) {
        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(appSecret)) {
            throw new IllegalArgumentException("'appKey' and 'appSecret' cannot be empty");
        }
        
        Response response = this.userService.getToken(
                TokenRequest.newBuilder()
                        .setAk(appKey)
                        .setSk(appSecret).build());

        return DubboClientUtils.deserialize(Token.class, response);
    }
}
