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
import io.github.airiot.sdk.client.service.core.UserClient;
import io.github.airiot.sdk.client.service.core.dto.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@HttpExchange
public interface UserClientImpl extends UserClient {

    @PostExchange("/core/user")
    @Override
    ResponseDTO<InsertResult> create(@Nonnull @RequestBody User user);

    @PatchExchange("/core/user/{userId}")
    @Override
    ResponseDTO<Void> update(@Nonnull @PathVariable("userId") String userId, @Nonnull @RequestBody  User user);

    @PutExchange("/core/user/{userId}")
    @Override
    ResponseDTO<Void> replace(@Nonnull @PathVariable("userId") String userId, @Nonnull @RequestBody User user);

    @DeleteExchange("/core/user/{userId}")
    @Override
    ResponseDTO<Void> deleteById(@Nonnull @PathVariable("userId") String userId);

    @GetExchange("/core/user")
    @Override
    ResponseDTO<List<User>> query(@Nonnull @GetObjectParams Query query);

    @GetExchange("/core/user/{userId}")
    @Override
    ResponseDTO<User> queryById(@Nonnull @PathVariable("userId") String userId);
}
