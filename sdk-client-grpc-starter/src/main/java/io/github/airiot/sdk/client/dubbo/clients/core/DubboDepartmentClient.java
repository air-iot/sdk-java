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
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dubbo.grpc.api.GetOrDeleteRequest;
import io.github.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import io.github.airiot.sdk.client.dubbo.grpc.api.Response;
import io.github.airiot.sdk.client.dubbo.grpc.core.DubboDeptServiceGrpc;
import io.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import io.github.airiot.sdk.client.service.core.DepartmentClient;
import io.github.airiot.sdk.client.service.core.dto.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.util.List;


public class DubboDepartmentClient implements DepartmentClient {

    private final Logger logger = LoggerFactory.getLogger(DubboDepartmentClient.class);
    private final DubboDeptServiceGrpc.IDeptService deptService;

    public DubboDepartmentClient(DubboDeptServiceGrpc.IDeptService deptService) {
        this.deptService = deptService;
    }

    @Override
    public ResponseDTO<List<Department>> query(@Nullable Query query) {
        if (query == null) {
            throw new IllegalArgumentException("the 'query' cannot be null");
        }

        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询部门信息: query = {}", new String(queryData));
        }

        Response response = this.deptService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询部门信息: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Department.class, response);
    }

    @Override
    public ResponseDTO<Department> queryById(@Nullable String departmentId) {
        if (!StringUtils.hasText(departmentId)) {
            throw new IllegalArgumentException("the 'departmentId' cannot be null or empty");
        }

        logger.debug("查询部门信息: departmentId = {}", departmentId);

        Response response = this.deptService.get(
                GetOrDeleteRequest.newBuilder().setId(departmentId).build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询部门信息: departmentId = {}, response = {}", departmentId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Department.class, response);
    }

    @Override
    public ResponseDTO<List<Department>> queryAll() {
        Query query = Query.newBuilder()
                .select(Department.class)
                .build();

        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询全部部门信息: query = {}", new String(queryData));
        }

        Response response = this.deptService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询全部部门信息: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Department.class, response);
    }
}
