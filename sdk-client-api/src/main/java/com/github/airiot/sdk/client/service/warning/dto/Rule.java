package com.github.airiot.sdk.client.service.warning.dto;


/**
 * 告警规则
 */
public class Rule {
    /**
     * 规则唯一标识
     */
    private String id;
    /**
     * 规则名称
     */
    private String name;
    /**
     * 规则类型
     */
    private String type;
    /**
     * 规则描述
     */
    private String description;
    /**
     * 应用范围
     */
    private String range;
    /**
     * 报警规则配置
     */
    private String settings;
    /**
     * 报警类型
     */
    private String warnType;
    /**
     * 报警级别
     */
    private String level;
    /**
     * warnIntervalObj
     */
    private String warnIntervalObj;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public String getWarnType() {
        return warnType;
    }

    public void setWarnType(String warnType) {
        this.warnType = warnType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getWarnIntervalObj() {
        return warnIntervalObj;
    }

    public void setWarnIntervalObj(String warnIntervalObj) {
        this.warnIntervalObj = warnIntervalObj;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", range='" + range + '\'' +
                ", settings='" + settings + '\'' +
                ", warnType='" + warnType + '\'' +
                ", level='" + level + '\'' +
                ", warnIntervalObj='" + warnIntervalObj + '\'' +
                '}';
    }
}

