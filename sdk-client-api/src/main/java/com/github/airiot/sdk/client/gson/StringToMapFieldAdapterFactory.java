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

package com.github.airiot.sdk.client.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

public final class StringToMapFieldAdapterFactory implements TypeAdapterFactory {

    private static final TypeToken<Map<String, Object>> TYPE_TOKEN = new TypeToken<Map<String, Object>>() {
    };

    static class StringToMapFieldAdapter extends TypeAdapter<Object> {
        private final Gson gson;

        public StringToMapFieldAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            out.value(this.gson.toJson(value));
//        Map<String, Object> data = this.gson.fromJson(value, TYPE_TOKEN);
//        out.beginObject();
//        out.endObject();
        }

        @Override
        public String read(JsonReader in) throws IOException {
            Object data = this.gson.fromJson(in, TYPE_TOKEN);
            return this.gson.toJson(data);
        }
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return (TypeAdapter<T>) new StringToMapFieldAdapter(gson);
    }
}
