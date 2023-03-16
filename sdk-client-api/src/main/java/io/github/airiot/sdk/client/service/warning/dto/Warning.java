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

package io.github.airiot.sdk.client.service.warning.dto;

import java.time.LocalDateTime;

public class Warning {
    /**
     * 报警唯一标识
     */
    private String id;
    /**
     * 报警等级
     */
    private String level;
    /**
     * 报警资产编号
     */
    private String uid;
    /**
     * 报警类型ID
     */
    private String type;
    /**
     * 报警确认状态
     */
    private String status;
    /**
     * 报警处理状态
     */
    private String processed;
    /**
     * 报警描述
     */
    private String desc;
    /**
     * 报警声音
     */
    private String audio;
    /**
     * 报警间隔
     */
    private Long interval;
    /**
     * 报警规则ID
     */
    private String ruleid;
    /**
     * 报警备注
     */
    private String remark;
    /**
     * 工作表ID
     */
    private String tableId;
    /**
     * 工作表记录ID
     */
    private String tableDataId;
    /**
     * 报警数据
     */
    private String fields;
    /**
     * 报警恢复时的数据
     */
    private String recoveryFields;
    /**
     * 创建时间
     */
    private LocalDateTime time;
    /**
     * 报警恢复时间
     */
    private LocalDateTime recoveryTime;
    /**
     * 报警确认时间
     */
    private LocalDateTime confirmTime;
    /**
     * 报警处理时间
     */
    private LocalDateTime handleTime;
    /**
     * 其他数据
     */
    private String other;
    /**
     * 报警数据点信息
     */
    private String warnTag;
    /**
     * 播放次数配置
     */
    private String timesOfPlay;
    /**
     * 是否需要处理
     */
    private Boolean handle;
    /**
     * 是否需要提醒
     */
    private Boolean alert;
    /**
     * 处理用户ID
     */
    private String handleUser;
    /**
     * 确认用户ID
     */
    private String confirmUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessed() {
        return processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public String getRuleid() {
        return ruleid;
    }

    public void setRuleid(String ruleid) {
        this.ruleid = ruleid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTableDataId() {
        return tableDataId;
    }

    public void setTableDataId(String tableDataId) {
        this.tableDataId = tableDataId;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getRecoveryFields() {
        return recoveryFields;
    }

    public void setRecoveryFields(String recoveryFields) {
        this.recoveryFields = recoveryFields;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public LocalDateTime getRecoveryTime() {
        return recoveryTime;
    }

    public void setRecoveryTime(LocalDateTime recoveryTime) {
        this.recoveryTime = recoveryTime;
    }

    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getWarnTag() {
        return warnTag;
    }

    public void setWarnTag(String warnTag) {
        this.warnTag = warnTag;
    }

    public String getTimesOfPlay() {
        return timesOfPlay;
    }

    public void setTimesOfPlay(String timesOfPlay) {
        this.timesOfPlay = timesOfPlay;
    }

    public Boolean getHandle() {
        return handle;
    }

    public void setHandle(Boolean handle) {
        this.handle = handle;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public String getHandleUser() {
        return handleUser;
    }

    public void setHandleUser(String handleUser) {
        this.handleUser = handleUser;
    }

    public String getConfirmUser() {
        return confirmUser;
    }

    public void setConfirmUser(String confirmUser) {
        this.confirmUser = confirmUser;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "id='" + id + '\'' +
                ", level='" + level + '\'' +
                ", uid='" + uid + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", processed='" + processed + '\'' +
                ", desc='" + desc + '\'' +
                ", audio='" + audio + '\'' +
                ", interval=" + interval +
                ", ruleid='" + ruleid + '\'' +
                ", remark='" + remark + '\'' +
                ", table='" + tableId + '\'' +
                ", tableData='" + tableDataId + '\'' +
                ", fields='" + fields + '\'' +
                ", recoveryFields='" + recoveryFields + '\'' +
                ", time=" + time +
                ", recoveryTime=" + recoveryTime +
                ", confirmTime=" + confirmTime +
                ", handleTime=" + handleTime +
                ", other='" + other + '\'' +
                ", warnTag='" + warnTag + '\'' +
                ", timesOfPlay='" + timesOfPlay + '\'' +
                ", handle=" + handle +
                ", alert=" + alert +
                ", handleUser='" + handleUser + '\'' +
                ", confirmUser='" + confirmUser + '\'' +
                '}';
    }
}

