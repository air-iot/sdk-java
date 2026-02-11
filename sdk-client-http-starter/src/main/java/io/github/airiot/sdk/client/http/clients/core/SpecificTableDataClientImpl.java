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

import com.google.gson.reflect.TypeToken;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.BatchInsertResult;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.UpdateOrDeleteResult;
import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.service.core.SpecificTableDataClient;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SpecificTableDataClientImpl<T> extends SpecificTableDataClient<T> {

    private final TableDataClientImpl client;

    public SpecificTableDataClientImpl(String tableId, Class<T> clazz,
                                       TableDataClientImpl client) {
        super(tableId, clazz);
        this.client = client;
    }

    private <E> ResponseDTO<E> parseResponse(ResponseDTO<String> response, Class<E> tClass) {
        if (!response.isSuccess() || response.getData() == null) {
            return response.to();
        } else if (String.class.isAssignableFrom(tClass)) {
            return response.to(tClass.cast(response.getData()));
        }
        E data = CustomGson.GSON.fromJson(response.getData(), tClass);
        return response.to(data);
    }

    private <E> ResponseDTO<List<E>> parseListResponse(ResponseDTO<String> response, Class<E> tClass) {
        if (!response.isSuccess() || response.getData() == null) {
            return response.to();
        }
        List<E> data = CustomGson.GSON.fromJson(response.getData(), TypeToken.getParameterized(List.class, tClass).getType());
        return response.to(data);
    }

    @Override
    public ResponseDTO<InsertResult> create(@NonNull T row) {
        ResponseDTO<String> response = this.client.create(this.getTableId(), row);
        return this.parseResponse(response, InsertResult.class);
    }

    @Override
    public ResponseDTO<BatchInsertResult> create(@NonNull List<T> rows) {
        ResponseDTO<String> response = this.client.create(this.getTableId(), rows);
        return this.parseResponse(response, BatchInsertResult.class);
    }

    @Override
    public ResponseDTO<UpdateOrDeleteResult> update(@NonNull String rowId, @NonNull T data) {
        ResponseDTO<String> response = this.client.update(this.getTableId(), rowId, data);
        return this.parseResponse(response, UpdateOrDeleteResult.class);
    }

    @Override
    public ResponseDTO<UpdateOrDeleteResult> update(@NonNull Query query, @NonNull T data) {
        ResponseDTO<String> response = this.client.update(this.getTableId(), query.getFilters(), data);
        return this.parseResponse(response, UpdateOrDeleteResult.class);
    }

    @Override
    public ResponseDTO<Void> replace(@NonNull String rowId, @NonNull T data) {
        ResponseDTO<String> response = this.client.replace(this.getTableId(), rowId, data);
        return this.parseResponse(response, Void.class);
    }

    @Override
    public ResponseDTO<Void> deleteById(@NonNull String rowId) {
        ResponseDTO<String> response = this.client.deleteById(this.getTableId(), rowId);
        return this.parseResponse(response, Void.class);
    }

    @Override
    public ResponseDTO<UpdateOrDeleteResult> deleteByQuery(@NonNull Query query) {
        ResponseDTO<String> response = this.client.deleteByQuery(this.getTableId(), query.getFilters());
        return this.parseResponse(response, UpdateOrDeleteResult.class);
    }

    @Override
    public ResponseDTO<List<T>> query(@NonNull Query query) {
        ResponseDTO<String> response = this.client.query(this.getTableId(), query);
        return this.parseListResponse(response, this.getClazz());
    }

    @Override
    public ResponseDTO<T> queryById(@NonNull String rowId) {
        ResponseDTO<String> response = this.client.queryById(this.getTableId(), rowId);
        return this.parseResponse(response, this.getClazz());
    }
}
