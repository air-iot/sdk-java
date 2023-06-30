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

package io.github.airiot.sdk.driver.data;

import io.github.airiot.sdk.driver.data.handlers.*;
import io.github.airiot.sdk.driver.model.Field;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.driver.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultDataHandlerChain implements DataHandlerChain {

    private final Logger logger = LoggerFactory.getLogger(DefaultDataHandlerChain.class);

    private final TagValueCache tagValueCache;
    private final List<DataHandler> handlers = new ArrayList<>();

    public DefaultDataHandlerChain(TagValueCache tagValueCache, List<DataHandler> handlers) {
        this(tagValueCache, handlers, true);
    }

    /**
     * 注: 如果 {@code registerDefaults} 为 {@code false} 则数据点中配置的 <b>有效范围</b>, <b>数值转换</b>, <b>小数位数</b> 和 <b>缩放比例</b> 配置将不生效.
     *
     * @param handlers         自定义数据处理功能
     * @param registerDefaults 是否启用平台默认数据处理功能. 如果设置为 {@code false} 则不启用.
     */
    public DefaultDataHandlerChain(TagValueCache tagValueCache, List<DataHandler> handlers, boolean registerDefaults) {
        this.tagValueCache = tagValueCache;
        if (registerDefaults) {
            this.registerDefaultHandlers();
        }
        handlers.sort(Comparator.comparing(DataHandler::getOrder));
        this.handlers.addAll(handlers);

        logger.info("数据处理 Chain: 是否注册默认数据处理功能: {}, 自定义数据处理功能: {}",
                registerDefaults,
                handlers.stream().map(handler -> handler.getClass().getName()).collect(Collectors.toList())
        );

        logger.info("数据处理 Chain: 数据处理功能执行顺序, {}",
                this.handlers.stream().map(handler -> handler.getClass().getName()).collect(Collectors.toList()));
    }

    /**
     * 注册平台默认数据点处理功能
     * <br>
     * 包括:
     * <br>
     * 1. 数值转换 <br>
     * 2. 小数位数和缩放比例 <br>
     * 3. 小数位数和缩放比例 v2 <br>
     * 4. 有效范围
     * 5. 将 boolean 转换为 0 或 1
     */
    public void registerDefaultHandlers() {
        this.handlers.add(new ConvertValueHandler());
        this.handlers.add(new RoundAndScaleValueHandler());
        this.handlers.add(new RangeValueHandler(this.tagValueCache));
        this.handlers.add(new RangeValueHandlerV2(this.tagValueCache));
        this.handlers.add(new BooleanToIntegerHandler());
    }

    @Override
    public <T extends Tag> Map<String, Object> handle(String tableId, String deviceId, T tag, Object value) {
        if (handlers.isEmpty()) {
            return Collections.singletonMap(tag.getId(), value);
        }

        Map<String, Object> finalValues = new HashMap<>(2);
        finalValues.put(tag.getId(), value);

        for (DataHandler handler : handlers) {
            Object tagValue = finalValues.get(tag.getId());
            if (!handler.supports(tableId, deviceId, tag, tagValue)) {
                logger.trace("数据处理: 跳过 [{}] 数据处理功能, tableId = {}, deviceId = {}, tag = {}, value = {}",
                        handler.getClass().getName(), tableId, deviceId, tag, tagValue);
                continue;
            }

            Map<String, Object> newValue = handler.handle(tableId, deviceId, tag, tagValue);
            if (newValue == null || newValue.isEmpty()) {
                logger.info("数据处理: 数据处理功能 [{}] 返回结果为 null, 中断数据处理. tableId = {}, deviceId = {}, tag = {}, value = {}",
                        handler.getClass().getName(), tableId, deviceId, tag, value);
                break;
            }

            finalValues.putAll(newValue);
            // 如果处理后的结果中不包含数据点, 则说明数据点的值被丢弃
            if (!newValue.containsKey(tag.getId())) {
                finalValues.remove(tag.getId());
            }
        }
        
        // 更新数据点最新有效值缓存
        if (finalValues.containsKey(tag.getId())) {
            this.tagValueCache.put(tableId, deviceId, tag.getId(), finalValues.get(tag.getId()));
        }

        return finalValues;
    }

    @Override
    public Point handle(Point point) {
        if (handlers.isEmpty()) {
            return point;
        }

        String tableId = point.getTable();
        String deviceId = point.getId();
        List<Field<? extends Tag>> finalFields = new ArrayList<>(point.getFields().size());
        for (Field<? extends Tag> field : point.getFields()) {
            if (field == null || field.getTag() == null) {
                logger.warn("数据处理: 数据中存在 tag 信息为 null 的数据. point = {}", point);
                continue;
            }

            Map<String, Object> newValue = this.handle(tableId, deviceId, field.getTag(), field.getValue());
            if (newValue == null || newValue.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, Object> entry : newValue.entrySet()) {
                if (entry.getKey().equals(field.getTag().getId())) {
                    // 必须创建新的 Field 对象, 如果直接修改可能会影响驱动中数据
                    finalFields.add(new Field<>(field.getTag(), entry.getValue()));
                    continue;
                }
                finalFields.add(new Field<>(new Tag(entry.getKey(), entry.getKey(), null, null, null, null), entry.getValue()));
            }
        }

        Point newPoint = new Point();
        newPoint.setTable(tableId);
        newPoint.setId(deviceId);
        newPoint.setCid(point.getCid());
        newPoint.setFields(finalFields);
        newPoint.setTime(point.getTime());
        newPoint.setFieldTypes(point.getFieldTypes());

        return newPoint;
    }
}
