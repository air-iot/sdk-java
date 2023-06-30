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
import io.github.airiot.sdk.client.service.core.dto.Role;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 角色客户端
 */
public interface RoleClient extends PlatformClient {

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 创建结果. 如果创建成功, 则返回角色ID
     */
    ResponseDTO<InsertResult> create(@Nonnull Role role);

    /**
     * 查询角色信息
     *
     * @param query 查询条件
     * @return 角色信息
     */
    ResponseDTO<List<Role>> query(@Nonnull Query query);

    /**
     * 根据角色ID查询角色信息
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    ResponseDTO<Role> queryById(@Nonnull String roleId);

    /**
     * 根据角色名称查询角色信息
     *
     * @param roleName 角色名称
     * @return 角色信息列表
     */
    default ResponseDTO<List<Role>> queryByName(@Nonnull String roleName) {
        return query(Query.newBuilder().select(Role.class).filter().eq(Role::getName, roleName).end().build());
    }

    /**
     * 查询全部角色信息
     *
     * @return 角色信息列表
     */
    default ResponseDTO<List<Role>> queryAll() {
        return query(Query.newBuilder().select(Role.class).build());
    }

    /**
     * 替换角色全部信息
     *
     * @param roleId 被替换角色ID
     * @param role   替换后的角色信息
     * @return 替换结果
     */
    ResponseDTO<Void> replace(@Nonnull String roleId, @Nonnull Role role);

    /**
     * 更新角色信息
     *
     * @param roleId 被更新角色ID
     * @param role   要替换的角色信息(值为 null 的字段不会被更新)
     * @return 更新结果
     */
    ResponseDTO<Void> update(@Nonnull String roleId, @Nonnull Role role);

    /**
     * 删除角色信息
     *
     * @param roleId 角色ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteById(@Nonnull String roleId);
}
