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

package io.github.airiot.sdk.driver.data.handlers;

import io.github.airiot.sdk.driver.data.DataHandler;
import io.github.airiot.sdk.driver.model.Tag;
import io.github.airiot.sdk.driver.model.TagValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

/**
 * 数据点-数值转换
 * <br>
 * 将当前数据点的值按在 {@code minRawValue} 和 {@code maxRawValue} 间位置(百分比, 即: value / (maxRawValue - minRawValue)) 映射到
 * {@code minValue} 和 {@code maxValue} 间相同百分比的值
 * <br>
 * <p>
 * 计算规则:
 * (当前值 - minRawValue) / (maxRawValue - minRawValue) * (maxValue - minValue) + minValue
 *
 * <pre>
 *     假设场景, 某温度计可测量温度范围为 -50.00 到 50.00, 精确到小数点后2位, 并且使用整数值表示温度(即接收到的数据为实际温度 * 100), 例如: 实际温度为 24.52, 接收到的值为 2452.
 *     使用映射功能将数值还原实际温度.
 *
 *     表示接收到的温度值测量范围: 温度值 * 100
 *     minRawValue: -5000
 *     maxRawValue: 5000
 *
 *     实际温度值测量范围
 *     minValue: -50
 *     maxValue: 50
 *
 *     1. 当接收到的温度值为 -5000 时, 映射后的值为 -50
 *     2. 当接收到的温度值为 0 时, 映射后的值为 0
 *     3. 当接收到的温度值为 5000 时, 映射后的值为 50
 *     4. 当接收到的温度值为 2358 时, 映射后的值为 23.58
 *     5. 当接收到的温度值为 -2358 时, 映射后的值为 -23.58
 * </pre>
 *
 * @see TagValue
 */
public class ConvertValueHandler implements DataHandler {

    private final Logger logger = LoggerFactory.getLogger(ConvertValueHandler.class);

    /**
     * 计算精度
     */
    private final int scale;

    public ConvertValueHandler(int scale) {
        this.scale = scale;
    }

    public ConvertValueHandler() {
        this(16);
    }

    @Override
    public boolean supports(String tableId, String deviceId, Tag tag, Object value) {
        if (!DataHandler.super.supports(tableId, deviceId, tag, value)) {
            return false;
        }

        if (!(value instanceof Number)) {
            return false;
        }

        TagValue mapping = tag.getTagValue();
        boolean matched = mapping != null && mapping.getMinValue() != null && mapping.getMaxValue() != null
                && mapping.getMinRaw() != null && mapping.getMaxRaw() != null;

        logger.debug("数据点数据处理器: 数值转换, table = {}, device = {}, tag = {}, mapping = {}, value = {}, match = {}",
                tableId, deviceId, tag.getId(), mapping, value, matched);

        return matched;
    }

    @Override
    public Map<String, Object> handle(String tableId, String deviceId, Tag tag, Object value) {
        double dValue = ((Number) value).doubleValue();
        if (!Double.isFinite(dValue)) {
            logger.warn("数据点数据处理器: 数值转换, 值为 {}, 丢弃. device = {}, tag = {}",
                    value, deviceId, tag.getId());
            return Collections.emptyMap();
        }

        TagValue tagValue = tag.getTagValue();

        BigDecimal val = BigDecimal.valueOf(dValue);

        BigDecimal minRawValue = BigDecimal.valueOf(tagValue.getMinRaw());
        BigDecimal maxRawValue = BigDecimal.valueOf(tagValue.getMaxRaw());
        BigDecimal minValue = BigDecimal.valueOf(tagValue.getMinValue());
        BigDecimal maxValue = BigDecimal.valueOf(tagValue.getMaxValue());

        if (minRawValue.equals(maxRawValue)) {
            logger.debug("数据点数据处理器: 数值转换, table = {}, device = {}, tag = {}, mapping = {}, value = {}, 最大值等于最小值, 无须处理",
                    tableId, deviceId, tag.getId(), tagValue, value);

            return Collections.singletonMap(tag.getId(), val);
        }

        val = val.max(minRawValue);
        val = val.min(maxRawValue);

        BigDecimal result = val.subtract(minRawValue)
                .divide(maxRawValue.subtract(minRawValue), this.scale, RoundingMode.HALF_DOWN)
                .multiply(maxValue.subtract(minValue)).add(minValue);

        logger.debug("数据点数据处理器: 数值转换, table = {}, device = {}, tag = {}, mapping = {}, value = {}, result = {}",
                tableId, deviceId, tag.getId(), tagValue, value, result);

        return Collections.singletonMap(tag.getId(), result.doubleValue());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
