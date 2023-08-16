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

package io.github.airiot.sdk.client.dubbo.clients.core;

import com.google.protobuf.ByteString;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dubbo.grpc.api.*;
import io.github.airiot.sdk.client.dubbo.grpc.core.DubboUserServiceGrpc;
import io.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import io.github.airiot.sdk.client.service.core.UserClient;
import io.github.airiot.sdk.client.service.core.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class DubboUserClient implements UserClient {

    private final Logger logger = LoggerFactory.getLogger(DubboUserClient.class);

    private final DubboUserServiceGrpc.IUserService userService;

    public DubboUserClient(DubboUserServiceGrpc.IUserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseDTO<InsertResult> create(@Nonnull User user) {
        logger.debug("创建用户: {}", user);

        Response response = this.userService.create(CreateRequest.newBuilder()
                .setData(DubboClientUtils.serialize(user))
                .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("创建用户: user = {}, response = {}", user, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(InsertResult.class, response);
    }

    @Override
    public ResponseDTO<Void> update(@Nonnull String userId, @Nonnull User user) {
        if (logger.isDebugEnabled()) {
            logger.debug("更新用户信息: {}", user);
        }

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("the id of user cannot be empty");
        }

        ByteString data = DubboClientUtils.serializeWithoutId(user);
        if ("{}".equals(data.toString(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("The update content cannot be empty");
        }

        Response response = this.userService.update(
                UpdateRequest.newBuilder()
                        .setId(userId.trim())
                        .setData(DubboClientUtils.serializeWithoutId(user))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("更新用户信息: userId = {}, user = {}, update = {}, response = {}",
                    userId, user, data.toStringUtf8(), DubboClientUtils.serialize(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public ResponseDTO<Void> replace(@Nonnull String userId, @Nonnull User user) {
        if (logger.isDebugEnabled()) {
            logger.debug("替换用户信息: {}", user);
        }
        
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("the id of user cannot be empty");
        }

        ByteString data = DubboClientUtils.serializeWithoutId(user);
        if ("{}".equals(data.toString(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("The replace content cannot be empty");
        }

        Response response = this.userService.update(
                UpdateRequest.newBuilder()
                        .setId(userId.trim())
                        .setData(DubboClientUtils.serializeWithoutId(user))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("替换用户信息: userId = {}, user = {}, update = {}, response = {}",
                    userId, user, data.toStringUtf8(), DubboClientUtils.serialize(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public ResponseDTO<Void> deleteById(@Nonnull String userId) {
        logger.debug("删除用户: {}", userId);
        Response response = this.userService.delete(
                GetOrDeleteRequest.newBuilder().setId(userId).build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("删除用户: userId = {}, response = {}", userId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public ResponseDTO<List<User>> query(@Nonnull Query query) {
        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询用户: {}", new String(queryData, StandardCharsets.UTF_8));
        }

        Response response = this.userService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询用户: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(User.class, response);
    }

    @Override
    public ResponseDTO<User> queryById(@Nonnull String userId) {
        logger.debug("查询用户: userId = {}", userId);
        Response response = this.userService.get(GetOrDeleteRequest.newBuilder()
                .setId(userId)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询用户: userId = {}, response = {}", userId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(User.class, response);
    }
}
