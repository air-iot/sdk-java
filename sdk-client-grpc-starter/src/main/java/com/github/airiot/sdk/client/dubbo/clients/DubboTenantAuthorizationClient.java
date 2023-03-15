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

package com.github.airiot.sdk.client.dubbo.clients;

import com.github.airiot.sdk.client.annotation.NonProject;
import com.github.airiot.sdk.client.dto.Token;
import com.github.airiot.sdk.client.dubbo.grpc.api.Response;
import com.github.airiot.sdk.client.dubbo.grpc.api.TokenRequest;
import com.github.airiot.sdk.client.dubbo.grpc.spm.DubboUserServiceGrpc;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.exception.AuthorizationException;
import com.github.airiot.sdk.client.service.AuthorizationClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 租户级授权客户端
 */
@NonProject
public class DubboTenantAuthorizationClient implements AuthorizationClient {

    private final AtomicReference<Token> holder = new AtomicReference<>();
    private final DubboUserServiceGrpc.IUserService userService;
    private final String appKey;
    private final String appSecret;

    public DubboTenantAuthorizationClient(DubboUserServiceGrpc.IUserService userService, String appKey, String appSecret) {
        this.userService = userService;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    @Override
    public synchronized Token getToken() {
        Token token = this.holder.get();
        if (token != null && !token.isExpired(Duration.ofSeconds(60))) {
            return token;
        }

        Response response = this.userService.getToken(TokenRequest.newBuilder()
                .setAk(this.appKey)
                .setSk(this.appSecret)
                .build());

        token = DubboClientUtils.deserialize(Token.class, response)
                .unwrap(() -> new AuthorizationException(response.getCode(), response.getInfo(), response.getDetail()));
        this.holder.set(token);
        return token;
    }
}
