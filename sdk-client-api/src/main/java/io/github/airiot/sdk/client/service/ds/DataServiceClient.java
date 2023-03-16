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

package io.github.airiot.sdk.client.service.ds;


import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * 数据接口客户端, 用于调用平台已创建的数据接口
 */
public interface DataServiceClient extends PlatformClient {

    /**
     * 调用数据接口
     *
     * @param tClass 接口返回值类型
     * @param dsId   接口标识
     * @param params 参数列表, 即数据接口中添加的参数, 如果没有定义参数则传 {@code null}. <br> key: 参数名. <br> value: 参数值.
     * @return 请求结果
     */
    <T> ResponseDTO<T> call(@Nonnull Class<T> tClass, @Nonnull String dsId, @Nullable Map<String, Object> params);

}
