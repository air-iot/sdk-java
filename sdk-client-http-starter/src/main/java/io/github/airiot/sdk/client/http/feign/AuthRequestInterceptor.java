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

package io.github.airiot.sdk.client.http.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.airiot.sdk.client.context.RequestContext;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.service.AuthorizationClient;
import io.github.airiot.sdk.client.service.Constants;

/**
 * 身份认证请求拦截器. 用于在请求头中添加身份认证信息
 */
public class AuthRequestInterceptor implements RequestInterceptor {

    private final AuthorizationClient authorizationClient;

    public AuthRequestInterceptor(AuthorizationClient authorizationClient) {
        this.authorizationClient = authorizationClient;
    }

    @Override
    public void apply(RequestTemplate template) {
        // 如果启用了身份认证
        if (RequestContext.isAuthEnabled()) {
            Token token = this.authorizationClient.getToken();
            template.header(Constants.HEADER_AUTHORIZATION, token.getToken());
        }
    }
}
