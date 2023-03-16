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
 * 数据点-数值转换配置
 */
public class TagValue {
    /**
     * 最小值
     */
    private Double minValue;
    /**
     * 最大值
     */
    private Double maxValue;
    /**
     * 原始最小值
     */
    private Double minRaw;
    /**
     * 原始最大值
     */
    private Double maxRaw;

    public TagValue() {
    }

    public TagValue(Double minValue, Double maxValue, Double minRaw, Double maxRaw) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minRaw = minRaw;
        this.maxRaw = maxRaw;
    }
    
    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getMinRaw() {
        return minRaw;
    }

    public void setMinRaw(Double minRaw) {
        this.minRaw = minRaw;
    }

    public Double getMaxRaw() {
        return maxRaw;
    }

    public void setMaxRaw(Double maxRaw) {
        this.maxRaw = maxRaw;
    }

    @Override
    public String toString() {
        return "TagValue{" +
                "minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", minRaw=" + minRaw +
                ", maxRaw=" + maxRaw +
                '}';
    }
}


