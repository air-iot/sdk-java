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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.BatchInsertResult;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.UpdateOrDeleteResult;
import io.github.airiot.sdk.client.http.clients.common.Context;
import io.github.airiot.sdk.client.http.clients.common.HttpCommonClient;

import java.util.List;
import java.util.Map;


public class TableDataCommonClient {

    private final Gson gson = new Gson();
    private final TypeToken<Map<String, Object>> MAP_TYPE = new TypeToken<Map<String, Object>>() {
    };
    private final TypeToken<List<Map<String, Object>>> MAP_LIST_TYPE = new TypeToken<List<Map<String, Object>>>() {
    };
    private final HttpCommonClient client;

    public TableDataCommonClient(HttpCommonClient client) {
        this.client = client;
    }

    public <T> ResponseDTO<InsertResult> create(String projectId, String tableId, T row) {
        Context context = Context.newBuilder().project(projectId).build();
        return this.client.post(context, String.format("/core/t/%s/d", tableId), row, InsertResult.class);
    }

    public <T> ResponseDTO<BatchInsertResult> create(String projectId, String tableId, List<T> rows) {
        Context context = Context.newBuilder().project(projectId).build();
        return this.client.post(context, String.format("/core/t/%s/d/many", tableId), rows, BatchInsertResult.class);
    }

    public <T> ResponseDTO<UpdateOrDeleteResult> update(String projectId, String tableId, String rowId, T data) {
        Context context = Context.newBuilder().project(projectId).build();
        return this.client.patch(context, String.format("/core/t/%s/d/%s", tableId, rowId), data, UpdateOrDeleteResult.class);
    }

    public <T> ResponseDTO<UpdateOrDeleteResult> update(String projectId, String tableId, Query query, T data) {
        Context context = Context.newBuilder().project(projectId).build();
        String filters = gson.toJson(query.getFilters());
        return this.client.patch(context, String.format("/core/t/%s/d/many?query=%s", tableId, filters), data, UpdateOrDeleteResult.class);
    }

    public <T> ResponseDTO<UpdateOrDeleteResult> update(String projectId, String tableId, Map<String, ?> query, T data) {
        Context context = Context.newBuilder().project(projectId).build();
        String filters = gson.toJson(query);
        return this.client.patch(context, String.format("/core/t/%s/d/many?query=%s", tableId, filters), data, UpdateOrDeleteResult.class);
    }

    public <T> ResponseDTO<UpdateOrDeleteResult> replace(String projectId, String tableId, String rowId, T data) {
        Context context = Context.newBuilder().project(projectId).build();
        return this.client.put(context, String.format("/core/t/%s/d/%s", tableId, rowId), data, UpdateOrDeleteResult.class);
    }

    public ResponseDTO<UpdateOrDeleteResult> deleteById(String projectId, String tableId, String rowId) {
        Context context = Context.newBuilder().project(projectId).build();
        return this.client.delete(context, String.format("/core/t/%s/d/%s", tableId, rowId), null, UpdateOrDeleteResult.class);
    }

    public ResponseDTO<UpdateOrDeleteResult> deleteByQuery(String projectId, String tableId, Query query) {
        Context context = Context.newBuilder().project(projectId).build();
        String filters = gson.toJson(query.getFilters());
        return this.client.delete(context, String.format("/core/t/%s/d/many?query=%s", tableId, filters), null, UpdateOrDeleteResult.class);
    }
    
    public ResponseDTO<Void> deleteByQuery(String projectId, String tableId, Map<String, ?> query) {
        Context context = Context.newBuilder().project(projectId).build();
        String filters = gson.toJson(query);
        return this.client.delete(context, String.format("/core/t/%s/d/many?query=%s", tableId, filters), null, Void.class);
    }

    public <T> ResponseDTO<List<T>> query(Class<T> tClass, String projectId, String tableId, Query query) {
        Context context = Context.newBuilder().project(projectId).build();
        String queryStr = gson.toJson(query);
        Class<?> clazz = TypeToken.getParameterized(List.class, tClass).getRawType();
        return (ResponseDTO<List<T>>) this.client.get(context, String.format("/core/t/%s/d?query=%s", tableId, queryStr), clazz);
    }

    public ResponseDTO<List<Map<String, Object>>> queryAsMap(String projectId, String tableId, Query query) {
        Context context = Context.newBuilder().project(projectId).build();
        String queryStr = gson.toJson(query);
        Class<?> clazz = MAP_LIST_TYPE.getRawType();
        return (ResponseDTO<List<Map<String, Object>>>) this.client.get(context, String.format("/core/t/%s/d?query=%s", tableId, queryStr), clazz);
    }

    public <T> ResponseDTO<T> queryById(Class<T> tClass, String projectId, String tableId, String rowId) {
        Context context = Context.newBuilder().project(projectId).build();
        return this.client.get(context, String.format("/core/t/%s/d/%s", tableId, rowId), tClass);
    }

    public ResponseDTO<Map<String, Object>> queryByIdAsMap(String projectId, String tableId, String rowId) {
        Context context = Context.newBuilder().project(projectId).build();
        return (ResponseDTO<Map<String, Object>>) this.client.get(context, String.format("/core/t/%s/d/%s", tableId, rowId), MAP_TYPE.getRawType());
    }
}
