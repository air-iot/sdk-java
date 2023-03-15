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
import com.github.airiot.sdk.client.dto.BatchInsertResult;
import com.github.airiot.sdk.client.dto.InsertResult;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.service.core.TableDataClient;
import com.github.airiot.sdk.client.dubbo.grpc.core.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;


public class DubboTableDataClient implements TableDataClient {

    private final Logger logger = LoggerFactory.getLogger(DubboTableDataClient.class);

    private final DubboTableDataServiceGrpc.ITableDataService tableDataService;

    public DubboTableDataClient(DubboTableDataServiceGrpc.ITableDataService tableDataService) {
        this.tableDataService = tableDataService;
    }

    private void checkTableId(String tableId) {
        if (!StringUtils.hasText(tableId)) {
            throw new IllegalArgumentException("'tableId' cannot be null or empty");
        }
    }

    private void checkRowId(String rowId) {
        if (!StringUtils.hasText(rowId)) {
            throw new IllegalArgumentException("'rowId' cannot be null or empty");
        }
    }

    @Override
    public <T> Response<InsertResult> create(@Nonnull String tableId, @Nonnull T row) {
        this.checkTableId(tableId);

        ByteString rowData = DubboClientUtils.serialize(row);

        if (logger.isDebugEnabled()) {
            logger.debug("添加工作表记录: tableId = {}, rowData = {}", tableId, rowData.toStringUtf8());
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.create(CreateDataRequest.newBuilder()
                .setTable(tableId)
                .setData(rowData)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("添加工作表记录: tableId = {}, rowData = {}, response = {}",
                    tableId, rowData.toStringUtf8(), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(InsertResult.class, response);
    }

    @Override
    public <T> Response<BatchInsertResult> create(@Nonnull String tableId, @Nonnull List<T> rows) {
        this.checkTableId(tableId);
        if (CollectionUtils.isEmpty(rows)) {
            throw new IllegalArgumentException("the 'rows' cannot be null");
        }

        ByteString rowsData = DubboClientUtils.serialize(rows);

        if (logger.isDebugEnabled()) {
            logger.debug("批量添加工作表记录: tableId = {}, rowsData = {}", tableId, rowsData.toStringUtf8());
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.create(CreateDataRequest.newBuilder()
                .setTable(tableId)
                .setData(rowsData)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("批量添加工作表记录: tableId = {}, rowsData = {}, response = {}",
                    tableId, rowsData.toStringUtf8(), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(BatchInsertResult.class, response);
    }

    @Override
    public <T> Response<Void> update(@Nonnull String tableId, @Nonnull String rowId, @Nonnull T data) {
        this.checkTableId(tableId);
        this.checkTableId(rowId);

        logger.debug("更新工作表记录: tableId = {}, rowId = {}, data = {}", tableId, rowId, data);

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.update(UpdateDataRequest.newBuilder()
                .setTable(tableId)
                .setId(rowId)
                .setData(DubboClientUtils.serialize(data))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("更新工作表记录: tableId = {}, rowId = {}, data = {}, response = {}",
                    tableId, rowId, data, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public <T> Response<Void> update(@Nonnull String tableId, @Nonnull Query query, @Nonnull T data) {
        this.checkTableId(tableId);

        byte[] queryData = query.serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("更新工作表记录: tableId = {}, query = {}, data = {}", tableId, new String(queryData), data);
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.updateMany(MultiUpdateDataRequest.newBuilder()
                .setTable(tableId)
                .setQuery(ByteString.copyFrom(query.serialize()))
                .setData(DubboClientUtils.serialize(data))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("更新工作表记录: tableId = {}, query = {}, data = {}, response = {}",
                    tableId, new String(queryData), data, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public <T> Response<Void> replace(@Nonnull String tableId, @Nonnull String rowId, @Nonnull T data) {
        this.checkTableId(tableId);
        this.checkTableId(rowId);

        logger.debug("替换工作表记录: tableId = {}, rowId = {}, data = {}", tableId, rowId, data);

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.replace(UpdateDataRequest.newBuilder()
                .setTable(tableId)
                .setId(rowId)
                .setData(DubboClientUtils.serialize(data))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("替换工作表记录: tableId = {}, rowId = {}, data = {}, response = {}",
                    tableId, rowId, data, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public Response<Void> deleteById(@Nonnull String tableId, @Nonnull String rowId) {
        this.checkTableId(tableId);
        this.checkRowId(rowId);

        logger.debug("删除工作表记录: tableId = {}, rowId = {}", tableId, rowId);

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.delete(GetOrDeleteDataRequest.newBuilder()
                .setTable(tableId)
                .setId(rowId)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("删除工作表记录: tableId = {}, rowId = {}, response = {}", tableId, rowId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public Response<Void> deleteByQuery(@Nonnull String tableId, @Nonnull Query query) {
        this.checkTableId(tableId);

        byte[] filter = query.serializeFilter();
        if (logger.isDebugEnabled()) {
            logger.debug("删除工作表记录: tableId = {}, query = {}", tableId, new String(filter));
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.deleteMany(QueryDataRequest.newBuilder()
                .setTable(tableId)
                .setQuery(ByteString.copyFrom(filter))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("删除工作表记录: tableId = {}, query = {}, response = {}", tableId, new String(filter), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public <T> Response<List<T>> query(@Nonnull Class<T> tClass, @Nonnull String tableId, @Nonnull Query query) {
        byte[] filter = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表记录: tableId = {}, query = {}", tableId, new String(filter));
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.query(QueryDataRequest.newBuilder()
                .setTable(tableId)
                .setQuery(ByteString.copyFrom(filter))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表记录: tableId = {}, query = {}, response = {}", tableId, new String(filter), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(tClass, response);
    }

    @Override
    public <T> Response<T> queryById(@Nonnull Class<T> tClass, @Nonnull String tableId, @Nonnull String rowId) {
        this.checkTableId(tableId);
        this.checkRowId(rowId);

        logger.debug("查询工作表记录: tableId = {}, rowId = {}, returnType = {}", tableId, rowId, tClass.getName());

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.get(GetOrDeleteDataRequest.newBuilder()
                .setTable(tableId)
                .setId(rowId)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表记录: tableId = {}, rowId = {}, response = {}", tableId, rowId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(tClass, response);
    }
}
