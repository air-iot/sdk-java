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
import com.github.airiot.sdk.client.dubbo.grpc.core.DubboRoleServiceGrpc;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.service.core.RoleClient;
import com.github.airiot.sdk.client.service.core.dto.Role;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;


public class DubboRoleClient implements RoleClient {

    private final Logger logger = LoggerFactory.getLogger(DubboRoleClient.class);

    private final DubboRoleServiceGrpc.IRoleService roleService;

    public DubboRoleClient(DubboRoleServiceGrpc.IRoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public Response<List<Role>> query(@Nonnull Query query) {
        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询角色信息: query = {}", new String(queryData));
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.roleService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询角色信息: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Role.class, response);
    }

    @Override
    public Response<Role> queryById(@Nonnull String roleId) {
        if (!StringUtils.hasText(roleId)) {
            throw new IllegalArgumentException("the 'roleId' cannot be null or empty");
        }

        logger.debug("查询角色信息: roleId = {}", roleId);

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.roleService.get(
                GetOrDeleteRequest.newBuilder().setId(roleId).build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询角色信息: roleId = {}, response = {}", roleId, DubboClientUtils.toString(response));
        }
        
        return DubboClientUtils.deserialize(Role.class, response);
    }
}
