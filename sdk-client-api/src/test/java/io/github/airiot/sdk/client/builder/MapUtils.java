package io.github.airiot.sdk.client.builder;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {

    public static class Entry<V> {
        private final String key;
        private final V value;

        public Entry(String key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public static <V> MapUtils.Entry<V> entry(String key, V value) {
        return new Entry<>(key, value);
    }

    public static <V> Map<String, V> from(Entry<V>... entries) {
        Map<String, V> map = new HashMap<>(entries.length);
        for (Entry<V> entry : entries) {
            map.put(entry.key, entry.value);
        }
        return map;
    }
}
