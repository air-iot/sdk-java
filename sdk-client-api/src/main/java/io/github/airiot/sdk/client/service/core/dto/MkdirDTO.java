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

package io.github.airiot.sdk.client.service.core.dto;


/**
 * 媒体库创建目录请求参数
 */
public class MkdirDTO {
    /**
     * 上级目录名称
     */
    private String catalog;
    /**
     * 目录名称
     */
    private String dirName;

    public String getCatalog() {
        return catalog;
    }

    public String getDirName() {
        return dirName;
    }

    public MkdirDTO() {
    }

    public MkdirDTO(String catalog, String dirName) {
        this.catalog = catalog;
        this.dirName = dirName;
    }

    @Override
    public String toString() {
        return "MkdirDTO{" +
                "catalog='" + catalog + '\'' +
                ", dirName='" + dirName + '\'' +
                '}';
    }
}
