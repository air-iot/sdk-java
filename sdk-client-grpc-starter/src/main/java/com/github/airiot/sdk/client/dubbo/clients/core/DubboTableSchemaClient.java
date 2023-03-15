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

package com.github.airiot.sdk.client.dubbo.clients.core;

import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dubbo.grpc.api.GetOrDeleteRequest;
import com.github.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import com.github.airiot.sdk.client.dubbo.grpc.core.DubboTableSchemaServiceGrpc;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.service.core.TableSchemaClient;
import com.github.airiot.sdk.client.service.core.dto.table.TableSchema;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

public class DubboTableSchemaClient implements TableSchemaClient {

    private final Logger logger = LoggerFactory.getLogger(DubboTableSchemaClient.class);
    private final DubboTableSchemaServiceGrpc.ITableSchemaService tableSchemaService;

    public DubboTableSchemaClient(DubboTableSchemaServiceGrpc.ITableSchemaService tableSchemaService) {
        this.tableSchemaService = tableSchemaService;
    }

    @Override
    public Response<List<TableSchema>> query(@Nonnull Query query) {
        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表定义: query = {}", new String(queryData));
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableSchemaService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表定义: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(TableSchema.class, response);
    }

    @Override
    public Response<List<TableSchema>> queryAll() {
        Query query = Query.newBuilder().select(TableSchema.class).build();
        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询全部工作表定义: query = {}", new String(queryData));
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableSchemaService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询全部工作表定义: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(TableSchema.class, response);
    }

    @Override
    public Response<TableSchema> queryById(@Nonnull String tableId) {
        if (!StringUtils.hasText(tableId)) {
            throw new IllegalArgumentException("'tableId' cannot be null or empty");
        }

        logger.debug("查询工作表定义: tableId = {}", tableId);

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableSchemaService.get(
                GetOrDeleteRequest.newBuilder()
                        .setId(tableId)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表定义: tableId = {}, response = {}", tableId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(TableSchema.class, response);
    }
}
