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
import io.github.airiot.sdk.client.dto.BatchInsertResult;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * 工作表记录操作客户端
 * <br>
 * 用于对工作表中的数据进行增、删、改、查操作
 */
public interface TableDataClient extends PlatformClient {

    /**
     * 向工作表中添加记录
     *
     * @param tableId 工作表标识
     * @param row     记录
     * @param <T>     工作表对应实体类泛型
     * @return 数据添加结果
     */
    <T> ResponseDTO<InsertResult> create(@Nonnull String tableId, @Nonnull T row);

    /**
     * 向工作表中批量添加记录
     *
     * @param tableId 工作表标识
     * @param rows    记录列表
     * @param <T>     工作表对应实体类泛型
     * @return 数据添加结果
     */
    <T> ResponseDTO<BatchInsertResult> create(@Nonnull String tableId, @Nonnull List<T> rows);

    /**
     * 更新工作表记录
     *
     * @param tableId 工作表标识
     * @param rowId   记录ID
     * @param data    要更新的记录
     * @return 数据更新结果
     */
    <T> ResponseDTO<Void> update(@Nonnull String tableId, @Nonnull String rowId, @Nonnull T data);

    /**
     * 批量更新工作表记录.
     * <br>
     * 更新所有与 {@code query} 匹配的记录
     *
     * @param tableId 工作表标识
     * @param query   更新条件
     * @param data    要更新的数据
     * @return 数据更新结果
     */
    <T> ResponseDTO<Void> update(@Nonnull String tableId, @Nonnull Query query, @Nonnull T data);

    /**
     * 替换记录全部信息
     *
     * @param tableId 工作表标识
     * @param rowId   记录ID
     * @param data    替换后的记录信息
     */
    <T> ResponseDTO<Void> replace(@Nonnull String tableId, @Nonnull String rowId, @Nonnull T data);

    /**
     * 根据记录ID删除数据
     *
     * @param tableId 工作表标识
     * @param rowId   记录ID
     * @return 数据删除结果
     */
    ResponseDTO<Void> deleteById(@Nonnull String tableId, @Nonnull String rowId);

    /**
     * 批量删除工作表记录
     *
     * @param tableId 工作表标识
     * @param query   删除条件
     * @return 数据删除结果
     */
    ResponseDTO<Void> deleteByQuery(@Nonnull String tableId, @Nonnull Query query);

    /**
     * 根据条件查询用户信息
     *
     * @param tClass  工作表记录关联对象类型
     * @param tableId 工作表标识
     * @param query   查询条件
     * @return 用户信息或错误信息
     */
    <T> ResponseDTO<List<T>> query(@Nonnull Class<T> tClass, @Nonnull String tableId, @Nonnull Query query);

    default ResponseDTO<List<Map<String, Object>>> query(@Nonnull String tableId, @Nonnull Query query) {
        return this.query(MAP_TYPE, tableId, query);
    }

    /**
     * 根据ID查询记录信息
     *
     * @param tClass  工作表记录关联对象类型
     * @param tableId 工作表标识
     * @param rowId   记录ID
     * @return 记录信息或错误信息
     */
    <T> ResponseDTO<T> queryById(@Nonnull Class<T> tClass, @Nonnull String tableId, @Nonnull String rowId);
}
