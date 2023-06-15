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

import feign.Param;
import feign.RequestLine;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.UserClient;
import io.github.airiot.sdk.client.service.core.dto.User;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public interface UserFeignClient extends UserClient {

    @RequestLine("POST /core/user")
    @Override
    ResponseDTO<User> create(@NotNull User user);

    @RequestLine("PATCH /core/user/{userId}")
    @Override
    ResponseDTO<Void> update(@Nonnull @Param("userId") String userId, @NotNull User user);

    @RequestLine("PUT /core/user/{userId}")
    @Override
    ResponseDTO<Void> replace(@Nonnull @Param("userId") String userId, @NotNull User user);

    @RequestLine("DELETE /core/user/{userId}")
    @Override
    ResponseDTO<Void> deleteById(@NotNull @Param("userId") String userId);

    @RequestLine("GET /core/user?query={query}")
    @Override
    ResponseDTO<List<User>> query(@NotNull @Param(value = "query") Query query);

    @RequestLine("GET /core/user/{userId}")
    @Override
    ResponseDTO<User> queryById(@NotNull @Param("userId") String userId);
}
