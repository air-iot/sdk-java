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

package com.github.airiot.sdk.driver.data.handlers;


import com.github.airiot.sdk.driver.data.DataHandler;
import com.github.airiot.sdk.driver.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


/**
 * 数据点-小数位数和缩放比例
 * <br>
 * 将数据点的值进行缩放以及小数值保留处理.
 * <br>
 * 首先对数据进行缩放处理, 缩放后的处理结果再做保留小数位处理.
 *
 * <pre>
 *
 *     mod: 缩放处理, 即: 原始值 * mod.
 *          例如: 数据点的值为 123456, mod 值为 0.001 则最终值为 123.456, 如果 mod 值为 100 则最终值为 12345600.
 *
 *     fixed: 小数位保留处理, 即保留有效的小数位并做四舍五入处理.
 *          例如: 123.456 保留 2 位小数最终值为 123.46, 保留 1 位小数 最终值为 123.5
 *
 * </pre>
 *
 * @see Tag#getFixed() 小数位
 * @see Tag#getMod() 缩放比例
 */
public class RoundAndScaleValueHandler implements DataHandler {

    private final Logger logger = LoggerFactory.getLogger(RoundAndScaleValueHandler.class);

    @Override
    public boolean supports(String tableId, String deviceId, Tag tag, Object value) {
        if (!DataHandler.super.supports(tableId, deviceId, tag, value)) {
            logger.debug("数据点数据处理器: tag 或 value 为 null, table = {}, device = {}, tag = {}, value = {}, 不是数值, 跳过处理",
                    tableId, deviceId, tag, value);
            return false;
        }

        if (!(value instanceof Number)) {
            logger.debug("数据点数据处理器: 小数位数和缩放比例, table = {}, device = {}, tag = {}, fixed = {}, mod = {}, value = {}, 不是数值, 跳过处理",
                    tableId, deviceId, tag.getId(), tag.getFixed(), tag.getMod(), value);
            return false;
        }

        if (tag.getFixed() == null && tag.getMod() == null) {
            logger.debug("数据点数据处理器: 小数位数和缩放比例, table = {}, device = {}, tag = {}, fixed = {}, mod = {}, value = {}, 未配置, 跳过处理",
                    tableId, deviceId, tag.getId(), tag.getFixed(), tag.getMod(), value);
            return false;
        }

        return true;
    }

    @Override
    public Object handle(String tableId, String deviceId, Tag tag, Object value) {
        double dValue = ((Number) value).doubleValue();
        if (!Double.isFinite(dValue)) {
            logger.warn("数据点数据处理器: 小数位数和缩放比例, 值为 {}, 丢弃. device = {}, tag = {}",
                    value, deviceId, tag.getId());
            return null;
        }

        BigDecimal val = BigDecimal.valueOf(dValue);

        if (tag.getMod() != null) {
            val = val.multiply(BigDecimal.valueOf(tag.getMod()));
        }

        if (tag.getFixed() != null && tag.getFixed() >= 0) {
            int precision = String.valueOf(val.longValue()).length() + tag.getFixed();
            val = val.round(new MathContext(precision, RoundingMode.HALF_UP));
        }

        return val.doubleValue();
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
