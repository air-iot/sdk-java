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

import io.github.airiot.sdk.driver.model.Tag;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 报警信息构建器
 */
public class WarningBuilder {

    private String id;
    private String tableId;
    private String tableDataId;
    private String level;
    private String ruleId;
    private List<WarningField> fields = new ArrayList<>();
    private List<String> warningTypes;
    private String processed = WarningProcessed.UNPROCESSED.getValue();
    private String status = WarningStatus.UNCONFIRMED.getValue();
    private ZonedDateTime time = ZonedDateTime.now();
    private boolean alert = true;
    private boolean handle = true;
    private String description;

    /**
     * 设置报警ID
     *
     * @param id 报警ID
     */
    public WarningBuilder id(String id) {
        Assert.hasText(id, "报警ID不能为空");
        this.id = id;
        return this;
    }

    /**
     * 设置报警所属表标识
     *
     * @param tableId 报警所属表标识
     */
    public WarningBuilder tableId(String tableId) {
        Assert.hasText(tableId, "报警所属表标识不能为空");
        this.tableId = tableId;
        return this;
    }

    /**
     * 设置产生报警的设备的编号
     *
     * @param deviceId 设备的编号
     */
    public WarningBuilder deviceId(String deviceId) {
        Assert.hasText(deviceId, "产生报警设备的标识不能为空");
        this.tableDataId = deviceId;
        return this;
    }

    /**
     * 设置报警等级
     *
     * @param level 报警等级
     */
    public WarningBuilder level(String level) {
        Assert.hasText(level, "报警等级不能为空");
        this.level = level;
        return this;
    }

    /**
     * 设置报警规则ID
     *
     * @param ruleId 报警规则ID
     */
    public WarningBuilder ruleId(String ruleId) {
        Assert.hasText(ruleId, "报警规则ID不能为空");
        this.ruleId = ruleId;
        return this;
    }

    /**
     * 设置报警产生的时间, 默认使用当前系统所在时区
     *
     * @param time 报警产生的时间
     */
    public WarningBuilder time(LocalDateTime time) {
        Assert.notNull(time, "报警产生的时间不能为空");
        this.time = ZonedDateTime.of(time, ZoneOffset.systemDefault());
        return this;
    }

    /**
     * 设置报警产生的时间
     *
     * @param time 报警产生的时间
     */
    public WarningBuilder time(ZonedDateTime time) {
        Assert.notNull(time, "报警产生的时间不能为空");
        this.time = time;
        return this;
    }

    /**
     * 设置报警提醒
     *
     * @param alert 报警提醒. 如果为 {@code true} 则开启报警提醒, 否则关闭报警提醒
     */
    public WarningBuilder alert(boolean alert) {
        this.alert = alert;
        return this;
    }

    /**
     * 禁用报警提醒
     */
    public WarningBuilder disableAlert() {
        this.alert = false;
        return this;
    }

    /**
     * 设置报警是否需要处理
     *
     * @param handle 报警是否需要处理. 如果为 {@code true} 则需要处理, 否则不需要处理
     */
    public WarningBuilder handle(boolean handle) {
        this.handle = handle;
        return this;
    }

    /**
     * 禁用报警处理
     */
    public WarningBuilder disableHandle() {
        this.handle = false;
        return this;
    }

    /**
     * 设置报警类型
     *
     * @param warningTypes 报警类型
     */
    public WarningBuilder warningTypes(List<String> warningTypes) {
        Assert.notEmpty(warningTypes, "报警类型不能为空");
        if (warningTypes.stream().anyMatch(warningType -> !StringUtils.hasText(warningType))) {
            throw new IllegalArgumentException("报警类型不能为空");
        }

        this.warningTypes = warningTypes;
        return this;
    }

    /**
     * 设置报警类型
     *
     * @param warningTypes 报警类型
     */
    public WarningBuilder warningTypes(String... warningTypes) {
        Assert.notEmpty(warningTypes, "报警类型不能为空");
        List<String> list = new ArrayList<>();
        for (String warningType : warningTypes) {
            if (!StringUtils.hasText(warningType)) {
                throw new IllegalArgumentException("报警类型不能为空");
            }
            list.add(warningType);
        }
        this.warningTypes = list;
        return this;
    }

    /**
     * 设置报警的处理状态
     *
     * @param processed 报警的处理状态
     * @see WarningProcessed
     */
    public WarningBuilder processed(WarningProcessed processed) {
        Assert.notNull(processed, "报警的处理状态不能为空");
        this.processed = processed.getValue();
        return this;
    }

    /**
     * 设置报警的确认状态
     *
     * @param status 报警的确认状态
     * @see WarningStatus
     */
    public WarningBuilder status(WarningStatus status) {
        Assert.notNull(status, "报警的确认状态不能为空");
        this.status = status.getValue();
        return this;
    }

    /**
     * 设置报警关联的数据点信息
     *
     * @param tag   数据点信息
     * @param value 数据点的值
     */
    public WarningBuilder field(Tag tag, Object value) {
        Assert.notNull(tag, "报警关联的数据点信息不能为空");
        Assert.hasText(tag.getId(), "报警关联的数据点 ID 不能为空");

        this.fields.add(WarningField.create(tag.getId(), tag.getName(), value));
        return this;
    }

    /**
     * 设置报警关联的数据点信息
     *
     * @param id    数据点的标识. 必填
     * @param name  数据点的名称.
     * @param value 数据点的值
     */
    public WarningBuilder field(String id, String name, Object value) {
        Assert.hasText(id, "报警关联的数据点 ID 不能为空");
        return this.field(new Tag(id, name), value);
    }

    /**
     * 设置报警关联的数据点信息
     *
     * @param id    数据点的标识. 必填
     * @param value 数据点的值
     */
    public WarningBuilder field(String id, Object value) {
        Assert.hasText(id, "报警关联的数据点 ID 不能为空");
        return this.field(new Tag(id, null), value);
    }

    /**
     * 设置报警关联的数据点信息
     *
     * @param fields 报警关联的数据点信息
     */
    public WarningBuilder fields(List<WarningField> fields) {
        for (WarningField field : fields) {
            if (field == null) {
                throw new IllegalArgumentException("报警关联的数据点信息不能为空");
            }
            this.fields.add(field);
        }
        return this;
    }

    /**
     * 设置报警关联的数据点信息
     *
     * @param fields 报警关联的数据点信息
     */
    public WarningBuilder fields(WarningField... fields) {
        for (WarningField field : fields) {
            if (field == null) {
                throw new IllegalArgumentException("报警关联的数据点信息不能为空");
            }
            this.fields.add(field);
        }
        return this;
    }

    /**
     * 设置报警描述信息
     *
     * @param description 报警描述信息
     */
    public WarningBuilder description(String description) {
        this.description = description;
        return this;
    }

    public Warning build() {
        Assert.hasText(tableId, "报警设备所属表标识不能为空");
        Assert.hasText(tableDataId, "报警设备的编号不能为空");
        Assert.hasText(level, "报警等级不能为空");
        Assert.hasText(ruleId, "报警规则ID不能为空");
        Assert.notEmpty(warningTypes, "报警类型不能为空");
        Assert.hasText(description, "报警描述信息不能为空");

        Warning warning = new Warning();
        if (StringUtils.hasText(id)) {
            warning.setId(id);
        } else {
            warning.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        }

        warning.setTable(new Warning.Table(tableId));
        warning.setTableData(new Warning.TableData(tableDataId));
        warning.setLevel(level);
        warning.setRuleId(ruleId);
        warning.setFields(fields);
        warning.setWarningTypes(warningTypes);
        warning.setProcessed(processed);
        warning.setStatus(status);
        warning.setTime(time);
        warning.setAlert(alert);
        warning.setHandle(handle);
        warning.setDescription(description);

        return warning;
    }
}
