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
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.SystemVariableClient;
import io.github.airiot.sdk.client.service.core.dto.SystemVariable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SystemVariableFeignClient extends SystemVariableClient {

    @RequestLine("GET /core/systemVariable?query={query}")
    @Override
    ResponseDTO<List<SystemVariable>> query(@NotNull @Param("query") Query query);

    @RequestLine("GET /core/systemVariable/{id}")
    @Override
    ResponseDTO<SystemVariable> queryById(@NotNull @Param("id") String id);

    @RequestLine("POST /core/systemVariable")
    @Override
    ResponseDTO<InsertResult> create(@NotNull SystemVariable systemVariable);

    @RequestLine("PUT /core/systemVariable/{id}")
    @Override
    ResponseDTO<Void> replace(@NotNull @Param("id") String id, @NotNull SystemVariable systemVariable);

    @RequestLine("PATCH /core/systemVariable/{id}")
    @Override
    ResponseDTO<Void> update(@NotNull @Param("id") String id, @NotNull SystemVariable systemVariable);

    @RequestLine("DELETE /core/systemVariable/{id}")
    @Override
    ResponseDTO<Void> deleteById(@NotNull @Param("id") String id);
}
