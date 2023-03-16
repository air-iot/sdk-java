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

package io.github.airiot.sdk.client.service.core;


import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.annotation.DisableAuth;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;

import javax.annotation.Nonnull;

/**
 * 扩展应用客户端
 */
public interface AppClient extends PlatformClient {

    /**
     * 使用 {@code appKey} 和 {@code appSecret} 获取项目级 token
     *
     * @param appKey    扩展应用标识
     * @param appSecret 扩展应用密钥
     * @return token
     */
    @DisableAuth
    ResponseDTO<Token> getToken(@Nonnull String appKey, @Nonnull String appSecret);

}
