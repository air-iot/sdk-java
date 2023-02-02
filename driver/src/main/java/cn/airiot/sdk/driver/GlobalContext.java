package cn.airiot.sdk.driver;

import cn.airiot.sdk.driver.data.model.Field;
import cn.airiot.sdk.driver.data.model.FieldType;
import cn.airiot.sdk.driver.data.model.Point;
import cn.airiot.sdk.driver.data.model.Tag;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 全局上下文, 保存一些常用信息
 */
public final class GlobalContext {

    private final AtomicReference<Map<String, DeviceInfo>> devices = new AtomicReference<>(Collections.emptyMap());

    public void set(Map<String, DeviceInfo> devices) {
        Assert.notNull(devices, "devices cannot be null");
        this.devices.set(Collections.unmodifiableMap(devices));
    }

    /**
     * 获取设备信息
     *
     * @param deviceId 设备ID
     * @return 设备信息. 如果设备不存在则返回 {@link Optional#empty()}
     * @throws IllegalArgumentException 如果设备ID为 {@code null} 或空字符串
     */
    public Optional<DeviceInfo> getDevice(String deviceId) {
        Assert.hasText(deviceId, "the device id is empty");
        return Optional.ofNullable(this.devices.get().get(deviceId));
    }

    /**
     * 获取设备信息
     *
     * @param deviceId 设备ID
     * @return 设备信息. 如果设备不存在则返回 {@code null}
     * @throws IllegalArgumentException 如果设备ID为 {@code null} 或空字符串
     */
    public DeviceInfo getDeviceOrNull(String deviceId) {
        Assert.hasText(deviceId, "the device id is empty");
        return this.devices.get().get(deviceId);
    }

    /**
     * 根据数据点标识填充 {@code Field} 字段
     *
     * @param deviceId  数据点所属设备ID
     * @param tagValues 数据点的值
     * @return 字段信息列表
     */
    public List<Field> createFields(String deviceId, Map<String, Object> tagValues) {
        if (CollectionUtils.isEmpty(tagValues)) {
            throw new IllegalArgumentException("数据点信息不能为空");
        }

        DeviceInfo info = this.getDevice(deviceId).orElseThrow(() -> new IllegalStateException("设备不存在: " + deviceId));
        Map<String, Tag> tags = info.getTags();
        if (CollectionUtils.isEmpty(tags)) {
            throw new IllegalStateException("未找到设备上的数据点信息: " + deviceId);
        }

        List<Field> fields = new ArrayList<>(tagValues.size());
        for (Map.Entry<String, Tag> entry : tags.entrySet()) {
            String tagId = entry.getKey();
            if (!tags.containsKey(tagId)) {
                continue;
            }

            fields.add(new Field(tags.get(tagId), entry.getValue()));
        }
        return fields;
    }

    public Point createPoint(String deviceId, Map<String, Object> tagValues) {
        return this.createPoint(deviceId, null, null, 0, tagValues, Collections.emptyMap());
    }

    public Point createPoint(String deviceId, long time, Map<String, Object> tagValues) {
        return this.createPoint(deviceId, null, null, time, tagValues, Collections.emptyMap());
    }

    public Point createPoint(String deviceId, long time, Map<String, Object> tagValues, Map<String, FieldType> fieldTypes) {
        return this.createPoint(deviceId, null, null, time, tagValues, fieldTypes);
    }

    public Point createPoint(String deviceId, String childDeviceId, long time, Map<String, Object> tagValues) {
        return this.createPoint(deviceId, childDeviceId, null, time, tagValues, Collections.emptyMap());
    }

    /**
     * 创建 {@link Point} 对象, 根据 tagId 自动填充相关数据
     *
     * @param deviceId      设备ID
     * @param childDeviceId 子设备ID
     * @param table         设备所属表标识
     * @param time          数据产生的时间
     * @param tagValues     数据点的值. <br> key: 数据点ID(tagId). <br> value: 数据点的值.
     * @param fieldTypes    数据点的数据类型
     * @return Point 对象
     */
    public Point createPoint(String deviceId, String childDeviceId, String table, long time,
                             Map<String, Object> tagValues, Map<String, FieldType> fieldTypes) {
        if (!StringUtils.hasText(deviceId)) {
            throw new IllegalStateException("设备标识不能为空");
        }

        DeviceInfo info = this.getDevice(deviceId).orElseThrow(() -> new IllegalStateException("设备不存在: " + deviceId));
        Map<String, Tag> tags = info.getTags();
        if (CollectionUtils.isEmpty(tags)) {
            throw new IllegalStateException("未找到设备上的数据点信息: " + deviceId);
        }

        List<Field> fields = this.createFields(deviceId, tagValues);

        if (fields.isEmpty()) {
            throw new IllegalArgumentException("未定义的数据点: " + tags.keySet());
        }

        if (!StringUtils.hasText(table)) {
            table = info.getTableId();
        }

        return new Point(deviceId, childDeviceId, table, time, fields, fieldTypes);
    }
}
