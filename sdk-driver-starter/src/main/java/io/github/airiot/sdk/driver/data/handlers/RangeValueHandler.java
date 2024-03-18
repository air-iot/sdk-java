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
import io.github.airiot.sdk.driver.model.Range;
import io.github.airiot.sdk.driver.model.Tag;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.github.airiot.sdk.logger.driver.DriverModules;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;


/**
 * 数据点-有效范围
 * <br>
 * <p>
 * 当数据点的值超出设定范围后执行相应的处理逻辑
 * <pre>
 *  fixed: 使用设定的固定值.
 *  boundary: 使用边界值. 当值小于设定的最小值时取设定的最小值, 大于设定的最大值时取设定的最大值.
 *  discard: 丢弃掉该数据点的数据, 即不上报给平台.
 * </pre>
 *
 * @see Range
 */
public class RangeValueHandler implements DataHandler {

    private final Logger logger = LoggerFactory.withContext().module(DriverModules.WRITE_POINTS).getDynamicLogger(RangeValueHandler.class);

    private final Set<String> actions = new HashSet<>(Arrays.asList("fixed", "boundary", "discard"));

    private final TagValueCache tagValueCache;

    public RangeValueHandler(TagValueCache tagValueCache) {
        this.tagValueCache = tagValueCache;
    }

    @Override
    public boolean supports(String tableId, String deviceId, Tag tag, Object value) {
        if (!DataHandler.super.supports(tableId, deviceId, tag, value)) {
            logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. tag 或 value 为 null, 不支持处理", tableId, deviceId, tag == null ? null : tag.getId());
            return false;
        }

        boolean matched;
        String tagId = tag.getId();
        Range range = tag.getRange();

        matched = value instanceof Number
                && range != null && StringUtils.hasText(range.getActive())
                && range.getMinValue() != null && range.getMaxValue() != null;

        if (matched && !actions.contains(range.getActive())) {
            logger.warn("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 无效的动作 {}, 有效动作为: fixed, boundary, discard",
                    tableId, deviceId, tagId, range.getActive());
            return false;
        }

//        if (matched && "fixed".equals(range.getActive()) && range.getFixedValue() == null) {
//            logger.warn("数据点数据处理器: 有效范围处理, 未提供有效的固定值, table = {}, device = {}, tag = {}, range = {}",
//                    tableId, deviceId, tagId, range);
//            return false;
//        }

        logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 有效范围配置 {}, 数据点的值 {}, 条件是否满足 {}",
                tableId, deviceId, tagId, range, value, matched);

        return matched;
    }

    @Override
    public Map<String, Object> handle(String tableId, String deviceId, Tag tag, Object value) {
        double dValue = ((Number) value).doubleValue();
        if (Double.isNaN(dValue) || !Double.isFinite(dValue)) {
            logger.warn("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 数据点的值为 {}, 丢弃",
                    value, deviceId, tag.getId(), value);
            return Collections.emptyMap();
        }

        BigDecimal val = BigDecimal.valueOf(dValue);
        Range range = tag.getRange();

        BigDecimal minValue = BigDecimal.valueOf(range.getMinValue());
        BigDecimal maxValue = BigDecimal.valueOf(range.getMaxValue());

        String tagId = tag.getId();

        logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 有效范围配置 {}, 数据点的值 {}",
                tableId, deviceId, tagId, range, value);

        // 值在设定范围之内
        if (val.compareTo(minValue) >= 0 && val.compareTo(maxValue) <= 0) {
            logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 数据点的值 {} 在有效范围内[{} - {}], 无须处理",
                    tableId, deviceId, tagId, value, minValue, maxValue);
            return Collections.singletonMap(tag.getId(), val.doubleValue());
        }

        logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 数据点的值 {} 超出有效范围内[{} - {}]",
                tableId, deviceId, tagId, value, minValue, maxValue);

        Map<String, Object> tagValues = new HashMap<>(3);
        if ("save".equals(tag.getRange().getInvalidAction())) {
            tagValues.put(tag.getInvalidTagId(), value);
        }

        switch (range.getActive().toLowerCase()) {
            case "fixed":
                if (range.getFixedValue() == null) {
                    logger.warn("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 有效范围动作为固定值, 但未提供有效的固定值, 丢弃数据, {}",
                            tableId, deviceId, tagId, range);
                    return tagValues;
                }

                double fixedValue = range.getFixedValue();
                logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 数据点的值为 {}, 最终取固定值 {}",
                        tableId, deviceId, tagId, value, fixedValue);

                tagValues.put(tagId, fixedValue);
                return tagValues;
            case "boundary":
                double finalValue = (val.compareTo(minValue) < 0 ? minValue : maxValue).doubleValue();

                logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 数据点的值为 {}, 最终取边界值 {}",
                        tableId, deviceId, tagId, value, finalValue);

                tagValues.put(tagId, finalValue);
                return tagValues;
            case "latest":
                TagValueCache.CacheValue latestValue = this.tagValueCache.get(tableId, deviceId, tagId);
                logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 数据点的值为 {}, 取最新有效值 {}",
                        tableId, deviceId, tagId, value, latestValue);
                if (latestValue != null) {
                    tagValues.put(tagId, latestValue.getValue());
                } else {
                    tagValues.put(tagId, value);
                }
                return tagValues;
            case "discard":
                logger.debug("数据点[旧版有效范围]处理器: 设备表={},设备={},数据点={}. 丢弃数据 {}",
                        tableId, deviceId, tagId, value);
                return tagValues;
            default:
                throw new IllegalArgumentException("无法识别数据点 '" + tagId + "' 定义的有效范围处理动作: " + range.getActive());
        }
    }

    @Override
    public int getOrder() {
        return 200;
    }
}
