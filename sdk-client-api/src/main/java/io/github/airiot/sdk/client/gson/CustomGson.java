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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 自定义 Gson 对象.
 * 该对象注册一些适配器, 用于处理一些特殊的数据类型.
 */
public class CustomGson {

    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingStrategy(new CustomFieldNameStrategy())
            .create();
}
