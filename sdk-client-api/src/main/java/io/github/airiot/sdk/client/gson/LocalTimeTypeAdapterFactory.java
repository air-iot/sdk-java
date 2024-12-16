package io.github.airiot.sdk.client.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return (TypeAdapter<T>) new LocalDateTypeAdapter();
    }

    protected static class LocalDateTypeAdapter extends TypeAdapter<LocalTime> {

        private final static DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public void write(JsonWriter out, LocalTime value) throws IOException {
            out.value(DEFAULT_FORMATTER.format(value));
        }

        @Override
        public LocalTime read(JsonReader in) throws IOException {
            String value = in.nextString();
            if (!StringUtils.hasText(value)) {
                return null;
            }
            return LocalTime.parse(value, DEFAULT_FORMATTER);
        }
    }

}
