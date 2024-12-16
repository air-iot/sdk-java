package io.github.airiot.sdk.client.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return (TypeAdapter<T>) new LocalDateTypeAdapter();
    }

    protected static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

        private final static DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(DEFAULT_FORMATTER.format(value));
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            String value = in.nextString();
            if (!StringUtils.hasText(value)) {
                return null;
            }
            return LocalDate.parse(value, DEFAULT_FORMATTER);
        }
    }

}
