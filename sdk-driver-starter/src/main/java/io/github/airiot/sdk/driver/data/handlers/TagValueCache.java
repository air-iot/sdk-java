package io.github.airiot.sdk.driver.data.handlers;

import io.github.airiot.sdk.driver.event.DriverReloadApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 数据点最新有效值缓存
 */
public class TagValueCache implements ApplicationListener<DriverReloadApplicationEvent> {

    public static class CacheValue {
        /**
         * 缓存设备的属的工作表标识
         */
        private final String tableId;
        /**
         * 缓存所属设备ID
         */
        private final String deviceId;
        /**
         * 数据点ID
         */
        private final String tagId;
        /**
         * 缓存时间
         */
        private final LocalDateTime time;
        /**
         * 缓存值
         */
        private final Object value;

        public String getTableId() {
            return tableId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getTagId() {
            return tagId;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public Object getValue() {
            return value;
        }

        public CacheValue(String tableId, String deviceId, String tagId, Object value) {
            this.tableId = tableId;
            this.deviceId = deviceId;
            this.tagId = tagId;
            this.time = LocalDateTime.now();
            this.value = value;
        }

        @Override
        public String toString() {
            return "CacheValue{" +
                    "tableId='" + tableId + '\'' +
                    ", deviceId='" + deviceId + '\'' +
                    ", tagId='" + tagId + '\'' +
                    ", time=" + time +
                    ", value=" + value +
                    '}';
        }
    }

    private final Map<String, Map<String, CacheValue>> cacheValues = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(DriverReloadApplicationEvent event) {
        this.cacheValues.clear();
    }

    /**
     * 保存最新有效值
     *
     * @param tableId  工作表标识
     * @param deviceId 设备编号
     * @param tagId    数据点标识
     * @param value    最新有效值
     */
    public void put(String tableId, String deviceId, String tagId, Object value) {
        String key = String.format("%s|#|%s", tagId, deviceId);
        this.cacheValues.putIfAbsent(key, new ConcurrentHashMap<>());
        this.cacheValues.get(key).put(tagId, new CacheValue(tableId, deviceId, tagId, value));
    }

    /**
     * 获取最新有效值
     *
     * @param tableId  工作表标识
     * @param deviceId 设备编号
     * @param tagId    数据点标识
     * @return 最新有效值. 如果不存在, 则返回 {@code null}
     */
    public CacheValue get(String tableId, String deviceId, String tagId) {
        if (!StringUtils.hasText(tableId)) {
            throw new IllegalArgumentException("tableId is empty");
        }

        if (!StringUtils.hasText(deviceId)) {
            throw new IllegalArgumentException("deviceId is empty");
        }

        if (!StringUtils.hasText(tagId)) {
            throw new IllegalArgumentException("tagId is empty");
        }

        String key = String.format("%s|#|%s", tagId, deviceId);
        if (!this.cacheValues.containsKey(key)) {
            return null;
        }
        return this.cacheValues.get(key).get(tagId);
    }
}
