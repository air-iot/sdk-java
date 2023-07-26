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


/**
 * 数据点. 该类中的字段为公共属性, 如果在 {@code schema.js} 中扩展了 tag 信息, 需要自己实现编写子类并添加扩展的字段信息
 */
public class Tag {
    /**
     * 数据点标识
     */
    private String id;
    /**
     * 数据点名称
     */
    private String name;
    /**
     * 数据点-数值转换配置信息
     */
    private TagValue tagValue;
    /**
     * 数据点-有效范围值配置信息
     */
    private Range range;
    /**
     * 数据点-小数位数配置
     */
    private Integer fixed;
    /**
     * 数据点-缩放比例配置
     */
    private Double mod;

    /**
     * 无效值的标识
     */
    public String getInvalidTagId() {
        return String.format("%s__invalid", id);
    }
    
    /**
     * 无效范围类型
     */
    public String getInvalidType() {
        return String.format("%s__invalid__type", id);
    }

    public Tag() {
    }

    public Tag(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Tag(String id, String name, TagValue tagValue, Range range, Integer fixed, Double mod) {
        this.id = id;
        this.name = name;
        this.tagValue = tagValue;
        this.range = range;
        this.fixed = fixed;
        this.mod = mod;
    }

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

    public TagValue getTagValue() {
        return tagValue;
    }

    public void setTagValue(TagValue tagValue) {
        this.tagValue = tagValue;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Integer getFixed() {
        return fixed;
    }

    public void setFixed(Integer fixed) {
        this.fixed = fixed;
    }

    public Double getMod() {
        return mod;
    }

    public void setMod(Double mod) {
        this.mod = mod;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tagValue=" + tagValue +
                ", range=" + range +
                ", fixed=" + fixed +
                ", mod=" + mod +
                '}';
    }
}
