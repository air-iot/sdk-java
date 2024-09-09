package io.github.airiot.sdk.datarelay.subscriber;

import java.util.Map;

/**
 * 订阅接收到的设备数据
 */
public class DeviceData {
    /**
     * 表标识
     */
    private String tableId;
    /**
     * 设备编号
     */
    private String deviceId;
    /**
     * 接收到数据的时间. 如果驱动发送的数据中包含 time 字段, 则使用该数据, 否则为当前系统时间
     */
    private long time;
    /**
     * 数据点的值.
     * <br>
     * key: 数据点标识<br>
     * value: 数据点的值
     */
    private Map<String, Object> fields;
    /**
     * 数据的类型.
     * <br>
     * key: 数据点标识<br>
     * value: 数据点的类型
     */
    private Map<String, FieldType> fieldTypes;

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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<String, FieldType> getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(Map<String, FieldType> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }

    @Override
    public String toString() {
        return "DeviceData{" +
                "tableId='" + tableId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", time=" + time +
                ", fields=" + fields +
                ", fieldTypes=" + fieldTypes +
                '}';
    }
}
