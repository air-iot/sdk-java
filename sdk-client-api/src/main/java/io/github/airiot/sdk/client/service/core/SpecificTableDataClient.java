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
import io.github.airiot.sdk.client.dto.UpdateOrDeleteResult;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 指定工作表的数据客户端
 * @param <T> 承载工作表记录的类型的泛型
 */
public abstract class SpecificTableDataClient<T> {

    private final String tableId;
    private final Class<T> clazz;

    public String getTableId() {
        return tableId;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public SpecificTableDataClient(String tableId, Class<T> clazz) {
        this.tableId = tableId;
        this.clazz = clazz;
    }

    /**
     * 向工作表中添加记录
     *
     * @param row 记录
     * @return 数据添加结果
     */
    public abstract ResponseDTO<InsertResult> create(@Nonnull T row);

    /**
     * 向工作表中批量添加记录
     *
     * @param rows 记录列表
     * @return 数据添加结果
     */
    public abstract ResponseDTO<BatchInsertResult> create(@Nonnull List<T> rows);

    /**
     * 更新工作表记录
     *
     * @param rowId 记录ID
     * @param data  要更新的记录
     * @return 数据更新结果
     */
    public abstract ResponseDTO<UpdateOrDeleteResult> update(@Nonnull String rowId, @Nonnull T data);

    /**
     * 批量更新工作表记录.
     * <br>
     * 更新所有与 {@code query} 匹配的记录
     *
     * @param query 更新条件
     * @param data  要更新的数据
     * @return 数据更新结果
     */
    public abstract ResponseDTO<UpdateOrDeleteResult> update(@Nonnull Query query, @Nonnull T data);

    /**
     * 替换记录全部信息
     *
     * @param rowId 记录ID
     * @param data  替换后的记录信息
     */
    public abstract ResponseDTO<Void> replace(@Nonnull String rowId, @Nonnull T data);

    /**
     * 根据记录ID删除数据
     *
     * @param rowId 记录ID
     * @return 数据删除结果
     */
    public abstract ResponseDTO<Void> deleteById(@Nonnull String rowId);

    /**
     * 批量删除工作表记录
     *
     * @param query 删除条件
     * @return 数据删除结果
     */
    public abstract ResponseDTO<UpdateOrDeleteResult> deleteByQuery(@Nonnull Query query);

    /**
     * 根据条件查询用户信息
     *
     * @param query 查询条件
     * @return 用户信息或错误信息
     */
    public abstract ResponseDTO<List<T>> query(@Nonnull Query query);

    /**
     * 根据ID查询记录信息
     *
     * @param rowId 记录ID
     * @return 记录信息或错误信息
     */
    public abstract ResponseDTO<T> queryById(@Nonnull String rowId);
}
