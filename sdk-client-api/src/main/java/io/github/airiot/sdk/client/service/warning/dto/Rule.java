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


import java.util.List;
import java.util.Map;

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
    private Map<String, Object> range;
    /**
     * 报警规则配置
     */
    private Map<String, Object> settings;
    /**
     * 报警类型
     */
    private List<String> warnType;
    /**
     * 报警级别
     */
    private String level;
    /**
     * warnIntervalObj
     */
    private Map<String, Object> warnIntervalObj;

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

    public Map<String, Object> getRange() {
        return range;
    }

    public void setRange(Map<String, Object> range) {
        this.range = range;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public List<String> getWarnType() {
        return warnType;
    }

    public void setWarnType(List<String> warnType) {
        this.warnType = warnType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Map<String, Object> getWarnIntervalObj() {
        return warnIntervalObj;
    }
    
    public void setWarnIntervalObj(Map<String, Object> warnIntervalObj) {
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

