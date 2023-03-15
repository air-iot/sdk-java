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

package com.github.airiot.sdk.driver.config;

import com.github.airiot.sdk.driver.model.Tag;

import java.util.List;


/**
 * 驱动基础配置信息
 * <br>
 * 如果驱动中的配置类为该类的子类时, 则会自动将驱动实例和模型中配置的数据点合并到设备中
 */
public class BasicConfig<T extends Tag> {

    /**
     * 数据点列表
     */
    private List<T> tags;

    public List<T> getTags() {
        return tags;
    }

    public void setTags(List<T> tags) {
        this.tags = tags;
    }
}
