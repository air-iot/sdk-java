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

package com.github.airiot.sdk.driver.data;

import com.github.airiot.sdk.driver.model.Field;
import com.github.airiot.sdk.driver.model.FieldType;
import com.github.airiot.sdk.driver.model.Point;
import com.github.airiot.sdk.driver.model.Tag;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * {@link Point} 序列化适配器
 */
public class PointSerializationAdapter extends TypeAdapter<Point> {

    public static TypeAdapterFactory newFactory() {
        return new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType().isAssignableFrom(Point.class)) {
                    return (TypeAdapter<T>) new PointSerializationAdapter(gson);
                }
                return null;
            }
        };
    }

    private final Gson gson;

    private PointSerializationAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, Point point) throws IOException {
        out.beginObject();

        List<Field<?>> fields = point.getFields();
        out.name("fields").beginObject();
        if (fields != null && !fields.isEmpty()) {
            for (Field<? extends Tag> field : fields) {
                if (field.getTag() == null) {
                    continue;
                }

                out.name(field.getTag().getId());
                Object value = field.getValue();
                if (value == null) {
                    out.nullValue();
                } else if (value instanceof Number) {
                    out.value((Number) value);
                } else if (value instanceof String) {
                    out.value((String) value);
                } else if (value instanceof Boolean) {
                    out.value((boolean) value);
                } else {
                    TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) this.gson.getAdapter(value.getClass());
                    typeAdapter.write(out, value);
                }
            }
        }
        out.endObject();

        out.name("source").value("device");
        out.name("cid").value(point.getCid());
        out.name("time").value(point.getTime());

        Map<String, FieldType> fieldTypes = point.getFieldTypes();
        if (fieldTypes != null && !fieldTypes.isEmpty()) {
            out.name("fieldTypes").beginObject();

            for (Map.Entry<String, FieldType> entry : fieldTypes.entrySet()) {
                out.name(entry.getKey()).value(entry.getValue().getValue());
            }
            
            out.endObject();
        }

        out.endObject();
    }

    @Override
    public Point read(JsonReader in) throws IOException {
        throw new IllegalStateException("Unsupported operation: deserialize Point");
    }
}
