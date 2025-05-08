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

import io.github.airiot.sdk.client.dto.RelatedTable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * 报警类型ID列表
     */
    private List<String> type;
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
    private RelatedTable table;
    /**
     * 工作表记录ID
     */
    private RelatedTable tableData;
    /**
     * 报警数据
     */
    private List<Map<String, Object>> fields;
    /**
     * 报警恢复时的数据
     */
    private List<Map<String, Object>> recoveryFields;
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
    private Map<String, Object> other;
    /**
     * 报警数据点信息
     */
    private Map<String, Object> warnTag;
    /**
     * 播放次数配置
     */
    private Integer timesOfPlay;
    /**
     * 是否需要处理
     */
    private Boolean handle;
    /**
     * 是否需要提醒
     */
    private Boolean alert;
    /**
     * 是否需要提醒
     */
    private Boolean audioAlert;
    /**
     * 处理用户ID
     */
    private RelatedTable handleUser;
    /**
     * 确认用户ID
     */
    private RelatedTable confirmUser;
    /**
     * 报警方式
     */
    private String warnMode;
    /**
     * 播报内容
     */
    private String broadcastContent;
    /**
     * 播报语音
     */
    private String broadcastVoice;
    /**
     * 国际化配置
     */
    private Map<String, Object> i18nProp;

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

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
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

    public RelatedTable getTable() {
        return table;
    }

    public void setTable(RelatedTable table) {
        this.table = table;
    }

    public RelatedTable getTableData() {
        return tableData;
    }

    public void setTableData(RelatedTable tableData) {
        this.tableData = tableData;
    }

    public List<Map<String, Object>> getFields() {
        return fields;
    }

    public void setFields(List<Map<String, Object>> fields) {
        this.fields = fields;
    }

    public List<Map<String, Object>> getRecoveryFields() {
        return recoveryFields;
    }

    public void setRecoveryFields(List<Map<String, Object>> recoveryFields) {
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

    public Map<String, Object> getOther() {
        return other;
    }

    public void setOther(Map<String, Object> other) {
        this.other = other;
    }

    public Map<String, Object> getWarnTag() {
        return warnTag;
    }

    public void setWarnTag(Map<String, Object> warnTag) {
        this.warnTag = warnTag;
    }

    public Integer getTimesOfPlay() {
        return timesOfPlay;
    }

    public void setTimesOfPlay(Integer timesOfPlay) {
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

    public Boolean getAudioAlert() {
        return audioAlert;
    }

    public void setAudioAlert(Boolean audioAlert) {
        this.audioAlert = audioAlert;
    }

    public RelatedTable getHandleUser() {
        return handleUser;
    }

    public void setHandleUser(RelatedTable handleUser) {
        this.handleUser = handleUser;
    }

    public RelatedTable getConfirmUser() {
        return confirmUser;
    }

    public void setConfirmUser(RelatedTable confirmUser) {
        this.confirmUser = confirmUser;
    }

    public String getWarnMode() {
        return warnMode;
    }

    public void setWarnMode(String warnMode) {
        this.warnMode = warnMode;
    }

    public String getBroadcastContent() {
        return broadcastContent;
    }

    public void setBroadcastContent(String broadcastContent) {
        this.broadcastContent = broadcastContent;
    }

    public String getBroadcastVoice() {
        return broadcastVoice;
    }

    public void setBroadcastVoice(String broadcastVoice) {
        this.broadcastVoice = broadcastVoice;
    }

    public Map<String, Object> getI18nProp() {
        return i18nProp;
    }

    public void setI18nProp(Map<String, Object> i18nProp) {
        this.i18nProp = i18nProp;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "id='" + id + '\'' +
                ", level='" + level + '\'' +
                ", uid='" + uid + '\'' +
                ", type=" + type +
                ", status='" + status + '\'' +
                ", processed='" + processed + '\'' +
                ", desc='" + desc + '\'' +
                ", audio='" + audio + '\'' +
                ", interval=" + interval +
                ", ruleid='" + ruleid + '\'' +
                ", remark='" + remark + '\'' +
                ", table=" + table +
                ", tableData=" + tableData +
                ", fields=" + fields +
                ", recoveryFields=" + recoveryFields +
                ", time=" + time +
                ", recoveryTime=" + recoveryTime +
                ", confirmTime=" + confirmTime +
                ", handleTime=" + handleTime +
                ", other=" + other +
                ", warnTag=" + warnTag +
                ", timesOfPlay=" + timesOfPlay +
                ", handle=" + handle +
                ", alert=" + alert +
                ", audioAlert=" + audioAlert +
                ", handleUser=" + handleUser +
                ", confirmUser=" + confirmUser +
                ", warnMode='" + warnMode + '\'' +
                ", broadcastContent='" + broadcastContent + '\'' +
                ", broadcastVoice='" + broadcastVoice + '\'' +
                ", i18nProp=" + i18nProp +
                '}';
    }
}

