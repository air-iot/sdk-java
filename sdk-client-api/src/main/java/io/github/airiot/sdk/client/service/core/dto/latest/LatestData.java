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

package io.github.airiot.sdk.client.service.core.dto.latest;

/**
 * 最新数据查询结果
 */
public class LatestData {
    /**
     * 工作表标识
     */
    private String tableId;
    /**
     * 资产编号
     */
    private String id;
    /**
     * 数据点标识
     */
    private String tagId;
    /**
     * 最后更新时间
     */
    private Long time;
    /**
     * 最新值
     */
    private Object value;

    public String getTableId() {
        return tableId;
    }

    public String getId() {
        return id;
    }

    public String getTagId() {
        return tagId;
    }

    public Long getTime() {
        return time;
    }

    public Object getValue() {
        return value;
    }

}
