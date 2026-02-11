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
import io.github.airiot.sdk.client.service.core.SystemVariableClient;
import io.github.airiot.sdk.client.service.core.dto.SystemVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@HttpExchange
public interface SystemVariableClientImpl extends SystemVariableClient {

    @GetExchange("/core/systemVariable?query={query}")
    @Override
    ResponseDTO<List<SystemVariable>> query(@Nonnull @GetObjectParams("query") Query query);

    @GetExchange("/core/systemVariable/{id}")
    @Override
    ResponseDTO<SystemVariable> queryById(@Nonnull @PathVariable("id") String id);

    @PostExchange("/core/systemVariable")
    @Override
    ResponseDTO<InsertResult> create(@Nonnull @RequestBody SystemVariable systemVariable);

    @PutExchange("/core/systemVariable/{id}")
    @Override
    ResponseDTO<Void> replace(@Nonnull @PathVariable("id") String id, @Nonnull @RequestBody SystemVariable systemVariable);

    @PatchExchange("/core/systemVariable/{id}")
    @Override
    ResponseDTO<Void> update(@Nonnull @PathVariable("id") String id, @Nonnull @RequestBody SystemVariable systemVariable);

    @DeleteExchange("/core/systemVariable/{id}")
    @Override
    ResponseDTO<Void> deleteById(@Nonnull @PathVariable("id") String id);
}
