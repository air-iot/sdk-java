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

import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.exception.AuthorizationException;
import io.github.airiot.sdk.client.service.AuthorizationClient;
import io.github.airiot.sdk.client.service.spm.SpmUserClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class HttpTenantAuthorizationClientImpl implements AuthorizationClient {

    private final AtomicReference<Token> holder = new AtomicReference<>();
    private final SpmUserClient spmUserClient;
    private final String appKey;
    private final String appSecret;

    public HttpTenantAuthorizationClientImpl(SpmUserClient spmUserClient, String appKey, String appSecret) {
        this.spmUserClient = spmUserClient;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    @Override
    public synchronized Token getToken() throws AuthorizationException {
        Token token = this.holder.get();
        if (token != null && !token.isExpired(Duration.ofSeconds(60))) {
            return token;
        }
        
        ResponseDTO<Token> response = this.spmUserClient.getToken(this.appKey, this.appSecret);
        token = response.unwrap(() -> new AuthorizationException(response.getCode(), response.getMessage(), response.getDetail()));

        this.holder.set(token);
        return token;
    }
}
