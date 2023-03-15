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
