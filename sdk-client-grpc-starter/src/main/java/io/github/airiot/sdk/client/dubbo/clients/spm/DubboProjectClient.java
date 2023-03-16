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

package io.github.airiot.sdk.client.dubbo.clients.spm;

import com.google.protobuf.ByteString;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dubbo.grpc.api.*;
import io.github.airiot.sdk.client.dubbo.grpc.spm.DubboProjectServiceGrpc;
import io.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import io.github.airiot.sdk.client.service.spm.ProjectClient;
import io.github.airiot.sdk.client.service.spm.dto.LicenseContent;
import io.github.airiot.sdk.client.service.spm.dto.Project;
import io.github.airiot.sdk.client.service.spm.dto.ProjectCreateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;


public class DubboProjectClient implements ProjectClient {

    private final Logger logger = LoggerFactory.getLogger(DubboProjectClient.class);

    private final DubboProjectServiceGrpc.IProjectService projectService;

    public DubboProjectClient(DubboProjectServiceGrpc.IProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ResponseDTO<ProjectCreateResult> create(@Nonnull Project project) {
        if (logger.isDebugEnabled()) {
            logger.debug("创建项目: project = {}", project);
        }

        ByteString projectData = DubboClientUtils.serialize(project);

        if (logger.isTraceEnabled()) {
            logger.trace("创建项目: project = {}", projectData.toStringUtf8());
        }

        Response responseDTO = this.projectService.create(CreateRequest.newBuilder()
                .setData(projectData)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("创建项目: project = {}, response = {}", project, DubboClientUtils.toString(responseDTO));
        }

        return DubboClientUtils.deserialize(ProjectCreateResult.class, responseDTO);
    }

    @Override
    public ResponseDTO<List<Project>> query(@Nonnull Query query) {
        if (!query.hasSelectFields()) {
            query = query.toBuilder().select(Project.class).build();
        }

        byte[] queryData = query.serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询项目: query = {}", new String(queryData));
        }

        Response responseDTO = this.projectService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询项目: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(responseDTO));
        }

        return DubboClientUtils.deserializeList(Project.class, responseDTO);
    }

    @Override
    public ResponseDTO<List<Project>> queryAll() {
        byte[] queryData = Query.newBuilder()
                .select(Project.class)
                .build()
                .serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询全部项目: query = {}", new String(queryData));
        }

        Response responseDTO = this.projectService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询全部项目: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(responseDTO));
        }

        return DubboClientUtils.deserializeList(Project.class, responseDTO);
    }

    @Override
    public ResponseDTO<Project> queryById(@Nonnull String projectId) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("'projectId' cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("查询项目: projectId = {}", projectId);
        }

        Response responseDTO = this.projectService.get(GetOrDeleteRequest.newBuilder()
                .setId(projectId)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询项目: projectId = {}, response = {}", projectId, DubboClientUtils.toString(responseDTO));
        }

        return DubboClientUtils.deserialize(Project.class, responseDTO);
    }

    @Override
    public ResponseDTO<Void> update(@Nonnull Project project) {
        if (!StringUtils.hasText(project.getId())) {
            throw new IllegalArgumentException("the project id cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("更新项目: projectId = {}, project = {}", project.getId(), project);
        }

        ByteString projectData = DubboClientUtils.serializeWithoutId(project);

        if (logger.isTraceEnabled()) {
            logger.debug("更新项目: projectId = {}, project = {}", project.getId(), projectData.toStringUtf8());
        }

        Response responseDTO = this.projectService.update(
                UpdateRequest.newBuilder()
                        .setId(project.getId())
                        .setData(projectData)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("更新项目: projectId = {}, project = {}, response = {}",
                    project.getId(), projectData.toStringUtf8(), DubboClientUtils.toString(responseDTO));
        }

        return DubboClientUtils.deserialize(Void.class, responseDTO);
    }

    @Override
    public ResponseDTO<Void> updateLicense(@Nonnull String projectId, @Nonnull LicenseContent license) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("the project id cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("更新项目授权信息: projectId = {}, license = {}", projectId, license);
        }

        ByteString licenseData = DubboClientUtils.serialize(license);

        if (logger.isTraceEnabled()) {
            logger.debug("更新项目授权信息: projectId = {}, license = {}", projectId, licenseData.toStringUtf8());
        }

        Response responseDTO = this.projectService.updateLicense(
                UpdateRequest.newBuilder()
                        .setId(projectId)
                        .setData(licenseData)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("更新项目授权信息: projectId = {}, license = {}, response = {}",
                    projectId, licenseData.toStringUtf8(), DubboClientUtils.toString(responseDTO));
        }

        return DubboClientUtils.deserialize(Void.class, responseDTO);
    }

    @Override
    public ResponseDTO<Void> replace(@Nonnull Project project) {
        if (!StringUtils.hasText(project.getId())) {
            throw new IllegalArgumentException("the project id cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("替换项目: projectId = {}, project = {}", project.getId(), project);
        }

        ByteString projectData = DubboClientUtils.serializeWithoutId(project);

        if (logger.isTraceEnabled()) {
            logger.debug("替换项目: projectId = {}, project = {}", project.getId(), projectData.toStringUtf8());
        }

        Response responseDTO = this.projectService.replace(
                UpdateRequest.newBuilder()
                        .setId(project.getId())
                        .setData(projectData)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("替换项目: projectId = {}, project = {}, response = {}",
                    project.getId(), projectData.toStringUtf8(), DubboClientUtils.toString(responseDTO));
        }

        return DubboClientUtils.deserialize(Void.class, responseDTO);
    }

    @Override
    public ResponseDTO<Void> deleteById(@Nonnull String projectId) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("'projectId' cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("删除项目: projectId = {}", projectId);
        }

        Response response = this.projectService.delete(GetOrDeleteRequest.newBuilder()
                .setId(projectId)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("删除项目: projectId = {}, response = {}", projectId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }
}
