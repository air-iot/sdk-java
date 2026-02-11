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

import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.http.annotation.GetObjectParams;
import io.github.airiot.sdk.client.service.spm.ProjectClient;
import io.github.airiot.sdk.client.service.spm.dto.Project;
import io.github.airiot.sdk.client.service.spm.dto.ProjectCreateResult;
import jakarta.annotation.Nonnull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

@HttpExchange
public interface SpmProjectClientImpl extends ProjectClient {

    @PostExchange("/spm/project")
    @Override
    ResponseDTO<ProjectCreateResult> create(@Nonnull @RequestBody Project project);

    @PatchExchange("/spm/project/{projectId}")
    ResponseDTO<Void> update(@Nonnull @PathVariable("projectId") String projectId, @Nonnull @RequestBody Project project);

    @PutExchange("/spm/project/{projectId}")
    ResponseDTO<Void> replace(@Nonnull @PathVariable("projectId") String projectId, @RequestBody Project project);

    @Override
    default ResponseDTO<Void> replace(@Nonnull Project project) {
        return this.replace(project.getId(), project);
    }

    @Override
    default ResponseDTO<Void> update(@Nonnull Project project) {
        return this.update(project.getId(), project);
    }

    @GetExchange("/spm/project")
    @Override
    ResponseDTO<List<Project>> query(@Nonnull @GetObjectParams Query query);

    @GetExchange("/spm/project/{projectId}")
    @Override
    ResponseDTO<Project> queryById(@Nonnull @PathVariable("projectId") String projectId);
    
    @DeleteExchange("/spm/project/{projectId}")
    @Override
    ResponseDTO<Void> deleteById(@Nonnull @PathVariable("projectId") String projectId);
}
