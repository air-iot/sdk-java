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

import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.http.annotation.GetObjectParams;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@HttpExchange
public interface TableDataClientImpl {

    @PostExchange("/core/t/{tableId}/d")
    <T> ResponseDTO<String> create(@Nonnull @PathVariable("tableId") String tableId, @Nonnull @RequestBody T row);

    @PostExchange("/core/t/{tableId}/d/many")
    <T> ResponseDTO<String> create(@Nonnull @PathVariable("tableId") String tableId, @Nonnull @RequestBody List<T> rows);

    @PatchExchange("/core/t/{tableId}/d/{rowId}")
    <T> ResponseDTO<String> update(@Nonnull @PathVariable("tableId") String tableId, @Nonnull @PathVariable("rowId") String rowId, @Nonnull @RequestBody T data);

    @PatchExchange("/core/t/{tableId}/d/many")
    <T> ResponseDTO<String> update(@Nonnull @PathVariable("tableId") String tableId, @Nonnull @GetObjectParams Map<String, ?> query, @Nonnull @RequestBody T data);

    @PutExchange("/core/t/{tableId}/d/{rowId}")
    <T> ResponseDTO<String> replace(@Nonnull @PathVariable("tableId") String tableId, @Nonnull @PathVariable("rowId") String rowId, @Nonnull @RequestBody T data);

    @DeleteExchange("/core/t/{tableId}/d/{rowId}")
    ResponseDTO<String> deleteById(@Nonnull @PathVariable("tableId") String tableId, @PathVariable("rowId") @Nonnull String rowId);

    @DeleteExchange("/core/t/{tableId}/d/many")
    ResponseDTO<String> deleteByQuery(@Nonnull @PathVariable("tableId") String tableId, @Nonnull @GetObjectParams Map<String, ?> query);

    @GetExchange("/core/t/{tableId}/d")
    ResponseDTO<String> query(@Nonnull @PathVariable("tableId") String tableId, @Nonnull @GetObjectParams Query query);
    
    @GetExchange("/core/t/{tableId}/d/{rowId}")
    ResponseDTO<String> queryById(@Nonnull @PathVariable("tableId") String tableId, @Nonnull @PathVariable("rowId") String rowId);
}
