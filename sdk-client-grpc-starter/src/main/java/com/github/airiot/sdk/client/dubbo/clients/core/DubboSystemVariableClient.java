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
import com.github.airiot.sdk.client.dubbo.grpc.core.DubboSystemVariableServiceGrpc;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.service.core.SystemVariableClient;
import com.github.airiot.sdk.client.service.core.dto.SystemVariable;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;


public class DubboSystemVariableClient implements SystemVariableClient {

    private final Logger logger = LoggerFactory.getLogger(DubboSystemVariableClient.class);
    private final DubboSystemVariableServiceGrpc.ISystemVariableService systemVariableService;

    public DubboSystemVariableClient(DubboSystemVariableServiceGrpc.ISystemVariableService systemVariableService) {
        this.systemVariableService = systemVariableService;
    }

    @Override
    public Response<List<SystemVariable>> query(@Nonnull Query query) {
        Query.Builder builder = query.toBuilder();
        if (!builder.containsSelectField(SystemVariable::getValue)) {
            builder.select(SystemVariable::getValue);
        }

        byte[] queryData = builder.build().serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: query = {}", new String(queryData));
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.systemVariableService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(SystemVariable.class, response);
    }

    @Override
    public Response<SystemVariable> queryByUId(@Nonnull String uid) {
        if (!StringUtils.hasText(uid)) {
            throw new IllegalArgumentException("'uid' cannot be null or empty");
        }

        byte[] queryData = Query.newBuilder()
                .select(SystemVariable.class)
                .eq(SystemVariable::getUid, uid)
                .build().serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: uid = {}", uid);
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.systemVariableService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: uid = {}, query = {}, response = {}", uid, new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(SystemVariable.class, response);
    }

    @Override
    public Response<SystemVariable> queryById(@Nonnull String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("'id' cannot be null or empty");
        }

        logger.debug("查询系统变量: id = {}", id);

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.systemVariableService.get(GetOrDeleteRequest.newBuilder()
                .setId(id)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: id = {}, response = {}", id, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(SystemVariable.class, response);
    }
}
