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
