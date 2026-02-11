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

package io.github.airiot.sdk.client.http.clients.core;

import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.http.annotation.GetObjectParams;
import io.github.airiot.sdk.client.service.core.RoleClient;
import io.github.airiot.sdk.client.service.core.dto.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

import java.util.List;

@HttpExchange
public interface RoleClientImpl extends RoleClient {

    @PostExchange("/core/role")
    @Override
    ResponseDTO<InsertResult> create(@NonNull @RequestBody Role role);

    @GetExchange("/core/role")
    @Override
    ResponseDTO<List<Role>> query(@NonNull @GetObjectParams Query query);

    @GetExchange("/core/role/{roleId}")
    @Override
    ResponseDTO<Role> queryById(@NonNull @PathVariable("roleId") String roleId);

    @PutExchange("/core/role/{roleId}")
    @Override
    ResponseDTO<Void> replace(@PathVariable("roleId") @NonNull String roleId, @RequestBody @NonNull Role role);

    @PatchExchange("/core/role/{roleId}")
    @Override
    ResponseDTO<Void> update(@PathVariable("roleId") @NonNull String roleId, @RequestBody @NonNull Role role);

    @DeleteExchange("/core/role/{roleId}")
    @Override
    ResponseDTO<Void> deleteById(@PathVariable("roleId") @NonNull String roleId);
}
