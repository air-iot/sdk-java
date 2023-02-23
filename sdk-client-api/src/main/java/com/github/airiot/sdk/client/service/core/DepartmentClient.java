package com.github.airiot.sdk.client.service.core;


import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.service.PlatformClient;
import com.github.airiot.sdk.client.service.core.dto.Department;
import com.github.airiot.sdk.client.dto.Response;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 部门客户端
 */
public interface DepartmentClient extends PlatformClient {

    /**
     * 查询部门信息
     *
     * @param query 查询条件
     * @return 部门信息
     */
    Response<List<Department>> query(@Nonnull Query query);

    /**
     * 根据部门ID查询部门信息
     *
     * @param departmentId 部门ID
     * @return 部门信息
     */
    Response<Department> queryById(@Nonnull String departmentId);
}
