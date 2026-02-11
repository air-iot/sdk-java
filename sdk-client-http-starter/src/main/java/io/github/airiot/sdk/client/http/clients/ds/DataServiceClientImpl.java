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

import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.service.ds.DataServiceClient;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

@HttpExchange
public interface DataServiceClientImpl extends DataServiceClient {

    /**
     * 调用数据接口
     *
     * @param dsId   数据接口ID
     * @param params 请求参数
     * @return 请求结果
     */
    @PostExchange("/ds/p/{dsId}")
    ResponseDTO<String> call(@NonNull @PathVariable("dsId") String dsId, @Nullable @RequestBody Map<String, Object> params);

    default <T> ResponseDTO<T> call(Class<T> clazz, @NonNull String dsId, @Nullable Map<String, Object> params) {
        ResponseDTO<String> response = this.call(dsId, params);
        if (!response.isSuccess() || !StringUtils.hasText(response.getData())) {
            return response.to();
        } else if (String.class.isAssignableFrom(clazz)) {
            return response.to((T) response.getData());
        }

        T data = CustomGson.GSON.fromJson(response.getData(), clazz);
        return new ResponseDTO<T>(response.isSuccess(), 0, response.getCode(), response.getMessage(), response.getDetail(), response.getField(), data);
    }
}
