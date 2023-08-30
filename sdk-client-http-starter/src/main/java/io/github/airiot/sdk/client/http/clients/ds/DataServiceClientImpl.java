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

package io.github.airiot.sdk.client.http.clients.ds;

import com.google.gson.Gson;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.ds.DataServiceClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 数据接口客户端实现
 */
public class DataServiceClientImpl implements DataServiceClient {

    private final Gson gson = new Gson();

    private final DataServiceFeignClient feignClient;

    public DataServiceClientImpl(DataServiceFeignClient dataServiceFeignClient) {
        this.feignClient = dataServiceFeignClient;
    }

    @Override
    public <T> ResponseDTO<T> call(@NotNull Class<T> tClass, @NotNull String dsId, @Nullable Map<String, Object> params) {
        ResponseDTO<String> response = this.feignClient.call(dsId, params);
        if (!response.isSuccess() || !StringUtils.hasText(response.getData())) {
            return response.to();
        }

        T data = this.gson.fromJson(response.getData(), tClass);
        return response.to(data);
    }
}
