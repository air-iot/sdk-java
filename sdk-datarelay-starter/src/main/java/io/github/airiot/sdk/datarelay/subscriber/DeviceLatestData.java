package io.github.airiot.sdk.datarelay.subscriber;

import java.util.List;

/**
 * 设备的数据点的最新数据
 */
public class DeviceLatestData {
    /**
     * 表标识
     */
    private String tableId;
    /**
     * 设备编号
     */
    private String deviceId;
    /**
     * 数据点列表.
     */
    private List<DeviceTagData> tags;

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<DeviceTagData> getTags() {
        return tags;
    }

    public void setTags(List<DeviceTagData> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "DeviceLatestData{" +
                "tableId='" + tableId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", tags=" + tags +
                '}';
    }

    public static class DeviceTagData {
        private final long time;
        private final String tagId;
        private final Object value;

        public String getTagId() {
            return tagId;
        }

        public long getTime() {
            return time;
        }

        public Object getValue() {
            return value;
        }

        public DeviceTagData(long time, String tagId, Object value) {
            this.time = time;
            this.tagId = tagId;
            this.value = value;
        }

        @Override
        public String toString() {
            return "DeviceTagData{" +
                    "time=" + time +
                    ", tagId='" + tagId + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
