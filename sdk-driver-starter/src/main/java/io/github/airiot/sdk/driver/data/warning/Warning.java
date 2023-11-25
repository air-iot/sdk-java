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

package io.github.airiot.sdk.driver.data.warning;


import com.google.gson.annotations.SerializedName;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 报警信息
 */
public class Warning {

    public static WarningBuilder builder() {
        return new WarningBuilder();
    }

    /**
     * 报警ID. 标识一条报警信息.
     * <br>
     * 如果不填写则自动生成一个UUID
     */
    private String id;
    /**
     * 产生报警的设备所属的表标识
     */
    private Table table;
    /**
     * 产生报警的设备的编号
     */
    private TableData tableData;
    /**
     * 告警等级
     */
    private String level;
    /**
     * 报警规则ID
     */
    @SerializedName("ruleid")
    private String ruleId;
    /**
     * 产生报警的数据点及值, 可选.
     * <br>
     * 如果为空, 则不关联到任何数据点
     */
    private List<WarningField> fields;
    /**
     * 报警类型
     */
    @SerializedName("type")
    private List<String> warningTypes;
    /**
     * 报警的处理状态
     */
    private String processed;
    /**
     * 报警的确认状态
     */
    private String status;
    /**
     * 报警产生的时间
     */
    private ZonedDateTime time;
    /**
     * 报警产生时是否需要提醒
     */
    private boolean alert;
    /**
     * 报警是否需要处理
     */
    private boolean handle;
    /**
     * 报警描述信息
     */
    @SerializedName("desc")
    private String description;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public List<WarningField> getFields() {
        return fields;
    }

    public void setFields(List<WarningField> fields) {
        this.fields = fields;
    }

    public List<String> getWarningTypes() {
        return warningTypes;
    }

    public void setWarningTypes(List<String> warningTypes) {
        this.warningTypes = warningTypes;
    }

    public String getProcessed() {
        return processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public boolean isHandle() {
        return handle;
    }

    public void setHandle(boolean handle) {
        this.handle = handle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "id='" + id + '\'' +
                ", table=" + table +
                ", tableData=" + tableData +
                ", level='" + level + '\'' +
                ", ruleId='" + ruleId + '\'' +
                ", fields=" + fields +
                ", warningTypes=" + warningTypes +
                ", processed=" + processed +
                ", status=" + status +
                ", time=" + time +
                ", alert=" + alert +
                ", handle=" + handle +
                ", description='" + description + '\'' +
                '}';
    }

    public static class Table {
        /**
         * 表标识
         */
        private final String id;


        public String getId() {
            return id;
        }

        public Table(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Table{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    public static class TableData {
        /**
         * 设备编号
         */
        private final String id;

        public String getId() {
            return id;
        }

        public TableData(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "TableData{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
