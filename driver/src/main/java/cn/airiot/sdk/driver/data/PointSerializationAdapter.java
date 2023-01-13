package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.model.Field;
import cn.airiot.sdk.driver.data.model.Point;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;


/**
 * {@link Point} 序列化适配器
 */
public class PointSerializationAdapter extends TypeAdapter<Point> {

    public static TypeAdapterFactory newFactory() {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked")
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

        out.name("fields").beginObject();
        for (Field field : point.getFields()) {
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
        out.endObject();
        
        out.name("source").value("device");
        out.name("cid").value(point.getCid());
        out.name("time").value(point.getTime());
        out.name("fieldTypes").beginObject();

        Map<String, String> fieldTypes = point.getFieldTypes();
        if (fieldTypes == null) {
            out.nullValue();
        } else if (!fieldTypes.isEmpty()) {
            for (Map.Entry<String, String> entry : fieldTypes.entrySet()) {
                out.name(entry.getKey()).value(entry.getValue());
            }
        }

        out.endObject();
        out.endObject();
    }

    @Override
    public Point read(JsonReader in) throws IOException {
        throw new IllegalStateException("Unsupported operation: deserialize Point");
    }
}
