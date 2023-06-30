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
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;
import io.github.airiot.sdk.client.service.core.dto.SystemVariable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 系统变量(数据字典)客户端
 */
public interface SystemVariableClient extends PlatformClient {

    /**
     * 查询系统变量信息
     *
     * @param query 查询条件
     * @return 系统变量信息
     */
    ResponseDTO<List<SystemVariable>> query(@Nonnull Query query);

    /**
     * 查询全部系统变量信息
     *
     * @return 系统变量信息
     */
    default ResponseDTO<List<SystemVariable>> queryAll() {
        return query(Query.newBuilder().select(SystemVariable.class).build());
    }

    /**
     * 根据系统变量编号查询系统变量信息
     *
     * @param uid 系统变量编号
     * @return 系统变量信息
     */
    default ResponseDTO<SystemVariable> queryByUId(@Nonnull String uid) {
        ResponseDTO<List<SystemVariable>> response = query(Query.newBuilder()
                .select(SystemVariable.class)
                .filter()
                .eq(SystemVariable::getUid, uid).end()
                .build());
        if (!response.isSuccess()) {
            return new ResponseDTO<>(response.isSuccess(), response.getCode(), response.getMessage(), response.getDetail(), null);
        }

        List<SystemVariable> systemVariables = response.getData();
        if (systemVariables != null && systemVariables.size() > 1) {
            throw new IllegalStateException("根据 uid '" + uid + "' 查询到多个系统变量, " + systemVariables);
        }

        return new ResponseDTO<>(response.isSuccess(), response.getCode(),
                response.getMessage(), response.getDetail(),
                systemVariables == null || systemVariables.isEmpty() ? null : systemVariables.get(0));
    }

    /**
     * 根据系统变量ID查询系统变量信息
     *
     * @param id 系统变量ID
     * @return 系统变量信息
     */
    ResponseDTO<SystemVariable> queryById(@Nonnull String id);

    /**
     * 根据系统变量名称查询系统变量信息
     *
     * @param name 系统变量名称
     * @return 系统变量信息
     */
    default ResponseDTO<List<SystemVariable>> queryByName(@Nonnull String name) {
        return query(Query.newBuilder().select(SystemVariable.class)
                .filter().eq(SystemVariable::getName, name).end()
                .build());
    }

    /**
     * 创建系统变量信息
     *
     * @param systemVariable 系统变量信息
     * @return 创建结果
     */
    ResponseDTO<InsertResult> create(@Nonnull SystemVariable systemVariable);

    /**
     * 替换系统变量信息
     *
     * @param id             系统变量ID
     * @param systemVariable 系统变量信息
     * @return 替换结果
     */
    ResponseDTO<Void> replace(@Nonnull String id, @Nonnull SystemVariable systemVariable);

    /**
     * 更新系统变量信息
     *
     * @param id             系统变量ID
     * @param systemVariable 系统变量信息
     * @return 更新结果
     */
    ResponseDTO<Void> update(@Nonnull String id, @Nonnull SystemVariable systemVariable);

    /**
     * 更新系统变量的值
     *
     * @param id    系统变量ID
     * @param value 系统变量值
     * @return 更新结果
     */
    default ResponseDTO<Void> updateValue(@Nonnull String id, @Nonnull Object value) {
        SystemVariable variable = new SystemVariable();
        variable.setId(id);
        variable.setValue(value);
        return this.update(id, variable);
    }

    /**
     * 删除系统变量信息
     *
     * @param id 系统变量ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteById(@Nonnull String id);
}
