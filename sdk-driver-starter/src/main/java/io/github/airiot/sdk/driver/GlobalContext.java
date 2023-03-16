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

package io.github.airiot.sdk.driver;

import io.github.airiot.sdk.driver.model.Field;
import io.github.airiot.sdk.driver.model.FieldType;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.driver.model.Tag;
import com.google.common.collect.Maps;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 全局上下文, 保存一些常用信息
 */
public final class GlobalContext {

    /**
     * deviceId: {tableId: DeviceInfo}
     */
    private final AtomicReference<Map<String, Map<String, DeviceInfo<? extends Tag>>>> devices = new AtomicReference<>(Collections.emptyMap());

    public void set(Map<String, List<DeviceInfo<? extends Tag>>> deviceInfos) {
        if (deviceInfos == null || deviceInfos.isEmpty()) {
            return;
        }

        Map<String, Map<String, DeviceInfo<? extends Tag>>> devices = Maps.newHashMapWithExpectedSize(deviceInfos.size());
        for (Map.Entry<String, List<DeviceInfo<? extends Tag>>> entry : deviceInfos.entrySet()) {
            devices.put(entry.getKey(), entry.getValue().stream()
                    .collect(Collectors.toMap(
                            DeviceInfo::getTableId,
                            dev -> dev
                    )));
        }
        this.devices.set(devices);
    }

    /**
     * 根据设备标识查找设备信息.
     *
     * @param deviceId 设备标识
     * @return 设备信息. 如果未找到设备则返回 {@link Optional#empty()}
     * @throws IllegalStateException 如果该设备标识在多个模型表中同时存在, 则会抛出该异常
     */
    public Optional<DeviceInfo<? extends Tag>> getDevice(String deviceId) {
        Assert.hasText(deviceId, "the device id is empty");
        if (!this.devices.get().containsKey(deviceId)) {
            return Optional.empty();
        }

        Map<String, DeviceInfo<? extends Tag>> infos = this.devices.get().get(deviceId);
        if (infos.size() != 1) {
            throw new IllegalStateException("在多个模型表中找到相同设备标识. deviceId =" + deviceId + ", tables = " + String.join(",", infos.keySet()));
        }

        return infos.values().stream().findFirst();
    }

    /**
     * 获取设备信息
     *
     * @param tableId  设备所属模型表标识
     * @param deviceId 设备ID
     * @return 设备信息. 如果设备不存在则返回 {@link Optional#empty()}
     * @throws IllegalArgumentException 如果设备ID为 {@code null} 或空字符串
     */
    public Optional<DeviceInfo<? extends Tag>> getDevice(String tableId, String deviceId) {
        Assert.hasText(deviceId, "the device id is empty");
        if (!this.devices.get().containsKey(deviceId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.devices.get().get(deviceId).get(tableId));
    }

    /**
     * 获取设备信息
     *
     * @param tableId  设备所属模型表标识
     * @param deviceId 设备ID
     * @return 设备信息. 如果设备不存在则返回 {@code null}
     * @throws IllegalArgumentException 如果设备ID为 {@code null} 或空字符串
     */
    public DeviceInfo<? extends Tag> getDeviceOrNull(String tableId, String deviceId) {
        return this.getDevice(tableId, deviceId).orElse(null);
    }

    /**
     * 根据数据点标识填充 {@code Field} 字段
     *
     * @param tableId   设备所属模型表标识
     * @param deviceId  数据点所属设备ID
     * @param tagValues 数据点的值
     * @return 字段信息列表
     */
    public List<Field<? extends Tag>> createFields(String tableId, String deviceId, Map<String, Object> tagValues) {
        if (CollectionUtils.isEmpty(tagValues)) {
            throw new IllegalArgumentException("数据点信息不能为空");
        }

        DeviceInfo<? extends Tag> info = this.getDevice(tableId, deviceId)
                .orElseThrow(() -> new IllegalStateException("设备不存在: " + deviceId));
        Map<String, ? extends Tag> tags = info.getTags();
        if (CollectionUtils.isEmpty(tags)) {
            throw new IllegalStateException("未找到设备上的数据点信息: " + deviceId);
        }

        List<Field<? extends Tag>> fields = new ArrayList<>(tagValues.size());
        for (Map.Entry<String, ? extends Tag> entry : tags.entrySet()) {
            String tagId = entry.getKey();
            if (!tags.containsKey(tagId)) {
                continue;
            }

            fields.add(new Field<>(tags.get(tagId), entry.getValue()));
        }
        return fields;
    }

    public Point createPoint(String tableId, String deviceId, Map<String, Object> tagValues) {
        return this.createPoint(tableId, deviceId, null, null, 0, tagValues, Collections.emptyMap());
    }

    public Point createPoint(String tableId, String deviceId, long time, Map<String, Object> tagValues) {
        return this.createPoint(tableId, deviceId, null, null, time, tagValues, Collections.emptyMap());
    }

    public Point createPoint(String tableId, String deviceId, long time, Map<String, Object> tagValues, Map<String, FieldType> fieldTypes) {
        return this.createPoint(tableId, deviceId, null, null, time, tagValues, fieldTypes);
    }

    public Point createPoint(String tableId, String deviceId, String childDeviceId, long time, Map<String, Object> tagValues) {
        return this.createPoint(tableId, deviceId, childDeviceId, null, time, tagValues, Collections.emptyMap());
    }

    /**
     * 创建 {@link Point} 对象, 根据 tagId 自动填充相关数据
     *
     * @param tableId       设备所属模型表标识
     * @param deviceId      设备ID
     * @param childDeviceId 子设备ID
     * @param table         设备所属表标识
     * @param time          数据产生的时间
     * @param tagValues     数据点的值. <br> key: 数据点ID(tagId). <br> value: 数据点的值.
     * @param fieldTypes    数据点的数据类型
     * @return Point 对象
     */
    public Point createPoint(String tableId, String deviceId, String childDeviceId, String table, long time,
                             Map<String, Object> tagValues, Map<String, FieldType> fieldTypes) {
        if (!StringUtils.hasText(deviceId)) {
            throw new IllegalStateException("设备标识不能为空");
        }

        List<Field<? extends Tag>> fields = this.createFields(tableId, deviceId, tagValues);

        if (fields.isEmpty()) {
            throw new IllegalArgumentException("未定义的数据点: " + tagValues.keySet());
        }

        if (!StringUtils.hasText(table)) {
            DeviceInfo<? extends Tag> info = this.getDevice(tableId, deviceId).orElseThrow(() -> new IllegalStateException("设备不存在: " + deviceId));
            table = info.getTableId();
        }

        return new Point(deviceId, childDeviceId, table, time, fields, fieldTypes);
    }
}
