package com.github.airiot.sdk.client.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

public final class MapToStringFieldAdapter extends TypeAdapter<Map<String, Object>> {

    private static final TypeToken<Map<String, Object>> TYPE_TOKEN = new TypeToken<Map<String, Object>>() {
    };

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return TYPE_TOKEN.getRawType().isAssignableFrom(typeToken.getRawType()) ? (TypeAdapter<T>) new MapToStringFieldAdapter(gson) : null;
        }
    };

    private final Gson gson;

    public MapToStringFieldAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, Map<String, Object> value) throws IOException {
        out.value(this.gson.toJson(value));
    }

    @Override
    public Map<String, Object> read(JsonReader in) throws IOException {
        return gson.fromJson(in, TYPE_TOKEN);
    }
}
