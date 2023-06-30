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
import feign.Response;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.http.feign.JsonParamExpander;
import io.github.airiot.sdk.client.http.feign.QueryParamExpander;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface TableDataFeignClient {

    @RequestLine("POST /core/t/{tableId}/d")
    <T> Response create(@Nonnull @Param("tableId") String tableId, @Nonnull T row);

    @RequestLine("POST /core/t/{tableId}/d/many")
    <T> Response create(@Nonnull @Param("tableId") String tableId, @Nonnull List<T> rows);

    @RequestLine("PATCH /core/t/{tableId}/d/{rowId}")
    <T> Response update(@Nonnull @Param("tableId") String tableId, @Nonnull @Param("rowId") String rowId, @Nonnull T data);

    @RequestLine("PATCH /core/t/{tableId}/d/many?query={query}")
    <T> Response update(@Nonnull @Param("tableId") String tableId, @Nonnull @Param(value = "query", expander = JsonParamExpander.class) Map<String, ?> query, @Nonnull T data);

    @RequestLine("PUT /core/t/{tableId}/d/{rowId}")
    <T> Response replace(@Nonnull @Param("tableId") String tableId, @Nonnull @Param("rowId") String rowId, @Nonnull T data);

    @RequestLine("DELETE /core/t/{tableId}/d/{rowId}")
    Response deleteById(@Nonnull @Param("tableId") String tableId, @Param("rowId") @Nonnull String rowId);

    @RequestLine("DELETE /core/t/{tableId}/d/many?query={query}")
    Response deleteByQuery(@Nonnull @Param("tableId") String tableId, @Nonnull @Param(value = "query", expander = JsonParamExpander.class) Map<String, ?> query);

    @RequestLine("GET /core/t/{tableId}/d?query={query}")
    Response query(@Nonnull @Param("tableId") String tableId, @Nonnull @Param(value = "query", expander = QueryParamExpander.class) Query query);

    @RequestLine("GET /core/t/{tableId}/d/{rowId}")
    Response queryById(@Nonnull @Param("tableId") String tableId, @Nonnull @Param("rowId") String rowId);
}
