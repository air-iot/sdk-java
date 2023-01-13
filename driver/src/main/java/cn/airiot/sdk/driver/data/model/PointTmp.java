package cn.airiot.sdk.driver.data.model;

import java.util.Map;

public class PointTmp {
    private Map<String, Object> fields;
    private long time;
    private Map<String, String> fieldTypes;

    public PointTmp() {
    }

    public PointTmp(Map<String, Object> fields, long time, Map<String, String> fieldTypes) {
        this.fields = fields;
        this.time = time;
        this.fieldTypes = fieldTypes;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Map<String, String> getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(Map<String, String> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }

    @Override
    public String toString() {
        return "PointTmp{" +
                "fields=" + fields +
                ", time=" + time +
                ", fieldTypes=" + fieldTypes +
                '}';
    }
}
