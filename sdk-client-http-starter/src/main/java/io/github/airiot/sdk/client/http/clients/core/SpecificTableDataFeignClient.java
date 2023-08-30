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
import feign.Response;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.BatchInsertResult;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.UpdateOrDeleteResult;
import io.github.airiot.sdk.client.exception.RequestFailedException;
import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.http.feign.ResponseError;
import io.github.airiot.sdk.client.service.Constants;
import io.github.airiot.sdk.client.service.core.SpecificTableDataClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SpecificTableDataFeignClient<T> extends SpecificTableDataClient<T> {

    private final TableDataFeignClient feignClient;

    public SpecificTableDataFeignClient(String tableId, Class<T> clazz,
                                        TableDataFeignClient feignClient) {
        super(tableId, clazz);
        this.feignClient = feignClient;
    }

    private <E> ResponseDTO<E> parseResponse(Response response, Class<E> tClass) {
        if (response.status() == 200) {
            if (tClass == Void.class) {
                return new ResponseDTO<>(true, 0, 200, "OK", "", null);
            }

            int count = 0;
            if (response.headers().containsKey(Constants.HEADER_COUNT)) {
                count = response.headers().get(Constants.HEADER_COUNT).stream()
                        .findFirst().map(Integer::parseInt).orElse(0);
            }

            try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
                E result = CustomGson.GSON.fromJson(reader, tClass);
                return new ResponseDTO<>(true, count, 200, "OK", "", result);
            } catch (IOException e) {
                throw new RequestFailedException(response.status(), "Failed to read response body", "", e);
            }
        }

        try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
            ResponseError error = CustomGson.GSON.fromJson(reader, ResponseError.class);
            return new ResponseDTO<>(false, 0, response.status(), error.getMessage(), error.getDetail(), error.getField(), null);
        } catch (IOException e) {
            throw new RequestFailedException(response.status(), "Failed to read response body", "", e);
        }
    }

    private <E> ResponseDTO<List<E>> parseListResponse(Response response, Class<E> tClass) {
        if (response.status() == 200) {
            int count = 0;
            if (response.headers().containsKey(Constants.HEADER_COUNT)) {
                count = response.headers().get(Constants.HEADER_COUNT).stream()
                        .findFirst().map(Integer::parseInt).orElse(0);
            }

            try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
                List<E> result = (List<E>) CustomGson.GSON.fromJson(reader, TypeToken.getParameterized(List.class, tClass));
                return new ResponseDTO<>(true, count, 200, "OK", "", result);
            } catch (IOException e) {
                throw new RequestFailedException(response.status(), "Failed to read response body", "", e);
            }
        }

        try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
            ResponseError error = CustomGson.GSON.fromJson(reader, ResponseError.class);
            return new ResponseDTO<>(false, 0, response.status(), error.getMessage(), error.getDetail(), error.getField(), null);
        } catch (IOException e) {
            throw new RequestFailedException(response.status(), "Failed to read response body", "", e);
        }
    }
    
    @Override
    public ResponseDTO<InsertResult> create(@NotNull T row) {
        Response response = this.feignClient.create(this.getTableId(), row);
        return this.parseResponse(response, InsertResult.class);
    }

    @Override
    public ResponseDTO<BatchInsertResult> create(@NotNull List<T> rows) {
        Response response = this.feignClient.create(this.getTableId(), rows);
        return this.parseResponse(response, BatchInsertResult.class);
    }

    @Override
    public ResponseDTO<UpdateOrDeleteResult> update(@NotNull String rowId, @NotNull T data) {
        Response response = this.feignClient.update(this.getTableId(), rowId, data);
        return this.parseResponse(response, UpdateOrDeleteResult.class);
    }

    @Override
    public ResponseDTO<UpdateOrDeleteResult> update(@NotNull Query query, @NotNull T data) {
        Response response = this.feignClient.update(this.getTableId(), query.getFilters(), data);
        return this.parseResponse(response, UpdateOrDeleteResult.class);
    }

    @Override
    public ResponseDTO<Void> replace(@NotNull String rowId, @NotNull T data) {
        Response response = this.feignClient.replace(this.getTableId(), rowId, data);
        return this.parseResponse(response, Void.class);
    }

    @Override
    public ResponseDTO<Void> deleteById(@NotNull String rowId) {
        Response response = this.feignClient.deleteById(this.getTableId(), rowId);
        return this.parseResponse(response, Void.class);
    }

    @Override
    public ResponseDTO<UpdateOrDeleteResult> deleteByQuery(@NotNull Query query) {
        Response response = this.feignClient.deleteByQuery(this.getTableId(), query.getFilters());
        return this.parseResponse(response, UpdateOrDeleteResult.class);
    }

    @Override
    public ResponseDTO<List<T>> query(@NotNull Query query) {
        Response response = this.feignClient.query(this.getTableId(), query);
        return this.parseListResponse(response, this.getClazz());
    }

    @Override
    public ResponseDTO<T> queryById(@NotNull String rowId) {
        Response response = this.feignClient.queryById(this.getTableId(), rowId);
        return this.parseResponse(response, this.getClazz());
    }
}
