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
 * 数据点-有效范围配置.
 * 如果采集到的数据不在 {@link Range#getMinValue()} 和 {@link Range#getMaxValue()} 范围内时, 会对数据做相关处理然后将处理后的结果数据发送到平台
 * <br>
 * 注: 只有采集到数据的类型为数值类型时才有效. 并且该处理过程已经集成在 sdk 中, 默认无须关注
 */
public class Range {
    /**
     * 最小值
     */
    private Double minValue;
    /**
     * 最大值
     */
    private Double maxValue;
    /**
     * 固定值, 仅当 {@link #active} 为 {@code fixed} 时有效
     */
    private Double fixedValue;
    /**
     * 当数据点采集到的数值超出设定范围时执行的动作. 可取值如下:
     * <br>
     * fixed: 固定值, 即返回 {@link #fixedValue} 定义的值
     * <br>
     * boundary: 边界值, 如果采集到的数据小于 {@link #minValue} 时返回 {@link #minValue}, 如果大于 {@link #maxValue} 时返回 {@link #maxValue}
     * <br>
     * discard: 丢弃, 即不上报该数据
     */
    private String active;

    public Range() {
    }

    public Range(Double minValue, Double maxValue, Double fixedValue, String active) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.active = active;
        this.fixedValue = fixedValue;
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

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Double getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(Double fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public String toString() {
        return "Range{" +
                "minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", active='" + active + '\'' +
                ", fixedValue=" + fixedValue +
                '}';
    }
}
