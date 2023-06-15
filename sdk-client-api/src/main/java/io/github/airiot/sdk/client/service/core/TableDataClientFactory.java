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

package io.github.airiot.sdk.client.service.core;

import io.github.airiot.sdk.client.annotation.WorkTable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作表记录客户端工厂, 可用于创建指定工作表记录客户端
 */
public abstract class TableDataClientFactory {

    private final Map<String, SpecificTableDataClient<?>> clients = new ConcurrentHashMap<>();

    /**
     * 根据工作表记录类创建指定工作表记录客户端. 要求该类必须标注 {@link WorkTable} 注解
     *
     * @param clazz 工作表记录类
     * @param <T>   工作表记录类型
     * @return 工作表记录客户端
     */
    public <T> SpecificTableDataClient<T> newClient(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(WorkTable.class)) {
            throw new IllegalArgumentException(clazz.getName() + " 未标注 @WorkTable 注解");
        }
        String tableId = clazz.getAnnotation(WorkTable.class).value();
        return newClient(tableId, clazz);
    }

    /**
     * 根据工作表记录类和表标识创建指定工作表记录客户端
     *
     * @param tableId 表标识
     * @param clazz   工作表记录类
     * @param <T>     工作表记录类型
     * @return 工作表记录客户端
     */
    public <T> SpecificTableDataClient<T> newClient(String tableId, Class<T> clazz) {
        return (SpecificTableDataClient<T>) this.clients.computeIfAbsent(tableId, key -> this.createClient(tableId, clazz));
    }

    protected abstract <T> SpecificTableDataClient<T> createClient(String tableId, Class<T> clazz);
}
