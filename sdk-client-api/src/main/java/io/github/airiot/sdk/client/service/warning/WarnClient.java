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

package io.github.airiot.sdk.client.service.warning;


import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;
import io.github.airiot.sdk.client.service.warning.dto.Warning;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 告警信息客户端
 */
public interface WarnClient extends PlatformClient {

    /**
     * 查询告警信息
     *
     * @param query   查询条件
     * @param archive 是否在归档数据中查询
     * @return 告警信息
     */
    ResponseDTO<List<Warning>> query(@Nonnull Query query, String archive);

    /**
     * 根据指定设备的所有告警信息
     *
     * @param deviceId 设备ID
     * @param archive  是否在归档数据中查询
     * @return 告警信息列表
     */
    default ResponseDTO<List<Warning>> queryByDeviceId(@Nonnull String deviceId, String archive) {
        return this.query(Query.newBuilder()
                .select(Warning.class)
                .filter()
                .eq("tableDataId", deviceId)
                .end()
                .build(), archive);
    }

    /**
     * 查询全部告警信息
     *
     * @param archive 是否在归档数据中查询
     * @return 告警信息列表
     */
    default ResponseDTO<List<Warning>> queryAll(String archive) {
        return this.query(Query.newBuilder()
                .select(Warning.class)
                .build(), archive);
    }

    /**
     * 创建告警
     *
     * @param warning 告警信息
     * @return 创建结果
     */
    ResponseDTO<InsertResult> create(@Nonnull Warning warning);
}
