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

package io.github.airiot.sdk.driver.data.warning;

import io.github.airiot.sdk.driver.model.Tag;
import org.springframework.util.Assert;

/**
 * 报警关联的数据点信息
 */
public class WarningField extends Tag {

    /**
     * 产生报警时数据点的值或者报警恢复时数据点的值
     */
    private final Object value;

    public Object getValue() {
        return value;
    }

    protected WarningField(String id, String name, Object value) {
        super(id, name);
        this.value = value;
    }

    public static WarningField create(String id, String name, Object value) {
        Assert.hasText(id, "数据点ID不能为空");
        return new WarningField(id, name, value);
    }

    public static WarningField create(String id, Object value) {
        return create(id, null, value);
    }
}
