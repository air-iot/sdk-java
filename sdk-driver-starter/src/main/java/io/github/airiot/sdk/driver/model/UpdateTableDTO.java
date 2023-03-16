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

import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * 更新设备信息
 *
 * @see DriverServiceGrpc#getUpdateTableDataMethod()
 */
public class UpdateTableDTO {
    /**
     * 工作表ID
     */
    private String table;
    /**
     * 设备编号
     */
    @SerializedName("id")
    private String rowId;
    /**
     * 要更新的属性列表
     * <br>
     * key: 字段名
     * value: 字段值
     */
    @SerializedName("custom")
    private Map<String, Object> fields;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public UpdateTableDTO() {
    }

    public UpdateTableDTO(String table, String rowId, Map<String, Object> fields) {
        this.table = table;
        this.rowId = rowId;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "UpdateTableDTO{" +
                "table='" + table + '\'' +
                ", rowId='" + rowId + '\'' +
                ", fields=" + fields +
                '}';
    }
}
