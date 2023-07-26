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
import io.github.airiot.sdk.client.service.core.dto.User;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 用户服务客户端
 */
public interface UserClient extends PlatformClient {

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 用户ID或错误信息
     */
    ResponseDTO<InsertResult> create(@Nonnull User user);

    /**
     * 更新用户信息
     *
     * @param userId 要更新的用户ID
     * @param user   要更新的用户信息
     * @return 更新结果
     */
    ResponseDTO<Void> update(@Nonnull String userId, @Nonnull User user);

    /**
     * 替换用户全部信息
     *
     * @param userId 要替换的用户ID
     * @param user   替换后的用户信息
     * @return 替换结果
     */
    ResponseDTO<Void> replace(@Nonnull String userId, @Nonnull User user);

    /**
     * 根据用户ID删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteById(@Nonnull String userId);

    /**
     * 根据条件查询用户信息
     *
     * @param query 查询条件
     * @return 用户信息或错误信息
     */
    ResponseDTO<List<User>> query(@Nonnull Query query);

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息或错误信息
     */
    ResponseDTO<User> queryById(@Nonnull String userId);

    /**
     * 根据用户名查询用户信息
     *
     * @param name 用户名
     * @return 用户信息或错误信息
     */
    default ResponseDTO<List<User>> queryByName(@Nonnull String name) {
        return query(Query.newBuilder().select(User.class).filter().eq(User::getName, name).end().build());
    }
}
