package cn.airiot.sdk.driver.data.model;

import java.util.List;
import java.util.Map;

public class Point {
    /**
     * 资产ID
     */
    private String id;
    /**
     * 子资产ID
     */
    private String cid;
    /**
     * 表ID
     */
    private String table;
    /**
     * 数据产生的时间
     */
    private long time;
    /**
     * 字段列表
     */
    private List<Field> fields;
    /**
     * 字段类型
     */
    private Map<String, String> fieldTypes;

    public Point() {
    }

    public Point(String id, List<Field> fields, long time, Map<String, String> fieldTypes) {
        this.id = id;
        this.fields = fields;
        this.time = time;
        this.fieldTypes = fieldTypes;
    }

    public Point(String id, String cid, List<Field> fields, long time, Map<String, String> fieldTypes) {
        this.id = id;
        this.cid = cid;
        this.fields = fields;
        this.time = time;
        this.fieldTypes = fieldTypes;
    }

    public Point(String id, String table, long time, List<Field> fields, Map<String, String> fieldTypes) {
        this.id = id;
        this.table = table;
        this.time = time;
        this.fields = fields;
        this.fieldTypes = fieldTypes;
    }

    public Point(String id, String cid, String table, long time, List<Field> fields, Map<String, String> fieldTypes) {
        this.id = id;
        this.cid = cid;
        this.table = table;
        this.time = time;
        this.fields = fields;
        this.fieldTypes = fieldTypes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
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
        return "Point{" +
                "id='" + id + '\'' +
                ", cid='" + cid + '\'' +
                ", table='" + table + '\'' +
                ", time=" + time +
                ", fields=" + fields +
                ", fieldTypes=" + fieldTypes +
                '}';
    }
}
