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

package io.github.airiot.sdk.client.http.clients.warn;

import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.http.annotation.GetObjectParams;
import io.github.airiot.sdk.client.service.warning.WarnClient;
import io.github.airiot.sdk.client.service.warning.dto.Warning;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 告警信息客户端
 */
@HttpExchange
public interface WarnClientImpl extends WarnClient {

    @GetExchange("/warning/warning")
    @Override
    ResponseDTO<List<Warning>> query(@Nonnull @GetObjectParams Query query, @RequestParam("archive") String archive);
    
    @PostExchange("/warning/warning")
    @Override
    ResponseDTO<InsertResult> create(@Nonnull @RequestBody Warning warning);
}
