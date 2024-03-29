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

package io.github.airiot.sdk.client.service.spm;


import io.github.airiot.sdk.client.annotation.NonProject;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;
import io.github.airiot.sdk.client.service.spm.dto.Project;
import io.github.airiot.sdk.client.service.spm.dto.ProjectCreateResult;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 项目客户端, 用于对平台中的项目进行增删改查等操作
 */
@NonProject
public interface ProjectClient extends PlatformClient {

    /**
     * 创建项目
     *
     * @param project 项目信息
     * @return 项目信息或错误信息
     */
    ResponseDTO<ProjectCreateResult> create(@Nonnull Project project);


    /**
     * 查询项目信息
     *
     * @param query 查询条件
     * @return 项目信息
     */
    ResponseDTO<List<Project>> query(@Nonnull Query query);

    /**
     * 查询全部项目信息
     *
     * @return 项目信息
     */
    default ResponseDTO<List<Project>> queryAll() {
        return query(Query.newBuilder().select(Project.class).build());
    }

    /**
     * 根据项目ID查询项目信息
     *
     * @param projectId 项目ID
     * @return 项目信息
     */
    ResponseDTO<Project> queryById(@Nonnull String projectId);

    /**
     * 更新项目信息
     * <br>
     * 如果字段的值为 {@code null} 则不更新
     *
     * @param project 更新后的项目信息
     * @return 更新结果
     */
    ResponseDTO<Void> update(@Nonnull Project project);

    /**
     * 替换项目信息
     *
     * @param project 替换后的项目信息
     * @return 替换结果
     */
    ResponseDTO<Void> replace(@Nonnull Project project);

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteById(@Nonnull String projectId);
}
