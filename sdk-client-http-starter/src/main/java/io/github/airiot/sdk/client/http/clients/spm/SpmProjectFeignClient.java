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

package io.github.airiot.sdk.client.http.clients.spm;

import feign.Param;
import feign.RequestLine;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.spm.ProjectClient;
import io.github.airiot.sdk.client.service.spm.dto.Project;
import io.github.airiot.sdk.client.service.spm.dto.ProjectCreateResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SpmProjectFeignClient extends ProjectClient {

    @RequestLine("POST /spm/project")
    @Override
    ResponseDTO<ProjectCreateResult> create(@NotNull Project project);

    @RequestLine("PATCH /spm/project/{projectId}")
    ResponseDTO<Void> update(@NotNull @Param("projectId") String projectId, Project project);

    @RequestLine("PUT /spm/project/{projectId}")
    ResponseDTO<Void> replace(@NotNull @Param("projectId") String projectId, Project project);

    @Override
    default ResponseDTO<Void> replace(@NotNull Project project) {
        return this.replace(project.getId(), project);
    }

    @Override
    default ResponseDTO<Void> update(@NotNull Project project) {
        return this.update(project.getId(), project);
    }

    @RequestLine("GET /spm/project?query={query}")
    @Override
    ResponseDTO<List<Project>> query(@NotNull @Param("query") Query query);

    @RequestLine("GET /spm/project/{projectId}")
    @Override
    ResponseDTO<Project> queryById(@NotNull @Param("projectId") String projectId);
    
    @RequestLine("DELETE /spm/project/{projectId}")
    @Override
    ResponseDTO<Void> deleteById(@NotNull String projectId);
}
