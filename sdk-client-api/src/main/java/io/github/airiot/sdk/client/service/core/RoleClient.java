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
import io.github.airiot.sdk.client.service.core.dto.Role;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 角色客户端
 */
public interface RoleClient extends PlatformClient {

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
}
