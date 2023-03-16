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

package io.github.airiot.sdk.client.dubbo.clients.ds;

import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dubbo.grpc.api.Response;
import io.github.airiot.sdk.client.dubbo.grpc.datasource.DubboDataServiceGrpc;
import io.github.airiot.sdk.client.dubbo.grpc.datasource.Request;
import io.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import io.github.airiot.sdk.client.service.ds.DataServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class DubboDataServiceClient implements DataServiceClient {

    private final Logger logger = LoggerFactory.getLogger(DubboDataServiceClient.class);

    private final DubboDataServiceGrpc.IDataService dataService;

    public DubboDataServiceClient(DubboDataServiceGrpc.IDataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public <T> ResponseDTO<T> call(@Nonnull Class<T> tClass, @Nonnull String dsId, @Nullable Map<String, Object> params) {
        if (!StringUtils.hasText(dsId)) {
            throw new IllegalArgumentException("'dsId' cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("调用数据接口: dsId = {}, params = {}, returnType = {}", dsId, params, tClass.getName());
        }

        Request.Builder builder = Request.newBuilder()
                .setKey(dsId);

        if (!CollectionUtils.isEmpty(params)) {
            builder.setData(
                    DubboClientUtils.serialize(params)
            );
        }

        Response response = this.dataService.proxy(builder.build());

        if (logger.isDebugEnabled()) {
            logger.debug("调用数据接口: dsId = {}, params = {}, response = {}", dsId, params, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(tClass, response);
    }
}
