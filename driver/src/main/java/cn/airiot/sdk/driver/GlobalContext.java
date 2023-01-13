package cn.airiot.sdk.driver;

import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
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
}
