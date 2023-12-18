/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.airiot.sdk.driver.model;

import io.github.airiot.sdk.driver.GlobalContext;

import java.util.List;
import java.util.Map;

/**
 * 数据点信息, 向平台上报数据的对象
 */
public class Point {
    /**
     * 资产ID.
     * <br>
     * 必填
     */
    private String id;
    /**
     * 子资产ID, 非必填
     */
    private String cid;
    /**
     * 表标识. 非必填.
     * <br>
     * 如果未填写时, 由 SDK 自动填充.
     * <br>
     * <b>注: 如果有多个模型内存在相同的设备编号时无法自动填充, 此时会抛出异常</b>
     */
    private String table;
    /**
     * 数据产生的时间. unix 时间戳(ms), 非必填
     * <br>
     * 如果为 {@code 0} 则默认为平台接收到数据的时间
     */
    private long time;
    /**
     * 数据点列表, 必填
     * <br>
     * 本次上报的数据. 可借助工具类方法构建.
     *
     * @see GlobalContext#createPoint(String, String, String, long, Map, Map)
     */
    private List<Field<? extends Tag>> fields;
    /**
     * 字段类型, 非必填.
     * <br>
     * key 为数据点标识.
     *
     * @see Tag#getId()
     */
    private Map<String, FieldType> fieldTypes;

    public Point() {
    }

    public Point(String id, List<Field<? extends Tag>> fields, long time, Map<String, FieldType> fieldTypes) {
        this.id = id;
        this.fields = fields;
        this.time = time;
        this.fieldTypes = fieldTypes;
    }

    public Point(String id, String cid, List<Field<? extends Tag>> fields, long time, Map<String, FieldType> fieldTypes) {
        this.id = id;
        this.cid = cid;
        this.fields = fields;
        this.time = time;
        this.fieldTypes = fieldTypes;
    }

    public Point(String id, String table, long time, List<Field<? extends Tag>> fields, Map<String, FieldType> fieldTypes) {
        this.id = id;
        this.table = table;
        this.time = time;
        this.fields = fields;
        this.fieldTypes = fieldTypes;
    }

    public Point(String id, String cid, String table, long time, List<Field<? extends Tag>> fields, Map<String, FieldType> fieldTypes) {
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

    public List<Field<? extends Tag>> getFields() {
        return fields;
    }

    public void setFields(List<Field<? extends Tag>> fields) {
        this.fields = fields;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Map<String, FieldType> getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(Map<String, FieldType> fieldTypes) {
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
