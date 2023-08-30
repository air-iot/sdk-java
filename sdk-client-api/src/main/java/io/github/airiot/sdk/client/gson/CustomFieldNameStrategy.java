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

package io.github.airiot.sdk.client.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;

/**
 * 自定义字段名称策略
 */
public class CustomFieldNameStrategy implements FieldNamingStrategy {

    @Override
    public String translateName(Field f) {
        if (f.isAnnotationPresent(io.github.airiot.sdk.client.annotation.Field.class)) {
            return f.getAnnotation(io.github.airiot.sdk.client.annotation.Field.class).value();
        }
        return FieldNamingPolicy.IDENTITY.translateName(f);
    }
}
