package com.github.airiot.sdk.client.dubbo.clients.core;

import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dubbo.grpc.api.GetOrDeleteRequest;
import com.github.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import com.github.airiot.sdk.client.dubbo.grpc.core.DubboDeptServiceGrpc;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.service.core.DepartmentClient;
import com.github.airiot.sdk.client.service.core.dto.Department;
import com.google.protobuf.ByteString;
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
    public Response<List<Department>> query(@Nullable Query query) {
        if (query == null) {
            throw new IllegalArgumentException("the 'query' cannot be null");
        }

        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询部门信息: query = {}", new String(queryData));
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.deptService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询部门信息: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Department.class, response);
    }

    @Override
    public Response<Department> queryById(@Nullable String departmentId) {
        if (!StringUtils.hasText(departmentId)) {
            throw new IllegalArgumentException("the 'departmentId' cannot be null or empty");
        }

        logger.debug("查询部门信息: departmentId = {}", departmentId);

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.deptService.get(
                GetOrDeleteRequest.newBuilder().setId(departmentId).build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询部门信息: departmentId = {}, response = {}", departmentId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Department.class, response);
    }
}
