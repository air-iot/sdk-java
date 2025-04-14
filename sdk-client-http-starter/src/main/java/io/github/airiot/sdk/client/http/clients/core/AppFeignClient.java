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


import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.service.core.AppClient;

import javax.annotation.Nonnull;
import java.util.Map;

public interface AppFeignClient extends AppClient {

    @RequestLine("GET /core/auth/token?appkey={appKey}&appsecret={appSecret}")
    @Override
    ResponseDTO<Token> getToken(@Nonnull @Param("appKey") String appKey, @Nonnull @Param("appSecret") String appSecret);

    @RequestLine("GET /core/auth/user")
    @Headers({
            "authorization: {token}",
            "x-request-project: {projectId}"
    })
    @Override
    ResponseDTO<Map<String, Object>> getUserAuth(@Param("token") String token, @Param("projectId") String projectId);
}
