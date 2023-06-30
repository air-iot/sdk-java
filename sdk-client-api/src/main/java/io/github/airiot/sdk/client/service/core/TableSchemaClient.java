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


import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;
import io.github.airiot.sdk.client.service.core.dto.table.TableSchema;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 工作表定义客户端
 */
public interface TableSchemaClient extends PlatformClient {

    /**
     * 查询工作表定义
     *
     * @return 工作表定义信息
     */
    ResponseDTO<List<TableSchema>> query(@Nonnull Query query);

    /**
     * 查询全部工作表定义
     *
     * @return 工作表定义信息
     */
    default ResponseDTO<List<TableSchema>> queryAll() {
        return query(Query.newBuilder().select(TableSchema.class).build());
    }

    /**
     * 查询工作表定义
     *
     * @param tableId 表标识
     * @return 工作表定义信息
     */
    ResponseDTO<TableSchema> queryById(@Nonnull String tableId);

    /**
     * 根据工作表标题查询工作表定义
     *
     * @param tableTitle 工作表标题
     * @return 工作表定义信息
     */
    default ResponseDTO<List<TableSchema>> queryByTitle(@Nonnull String tableTitle) {
        return query(Query.newBuilder().select(TableSchema.class).filter().eq(TableSchema::getTitle, tableTitle).end().build());
    }
}
