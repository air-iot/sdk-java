package io.github.airiot.sdk.datarelay.subscriber;

import org.springframework.context.SmartLifecycle;
import org.springframework.util.StringUtils;

import java.util.List;

public interface DataSubscriber extends SmartLifecycle {

    /**
     * 添加设备数据监听器
     *
     * @param listener 监听器
     */
    void listen(DataListener listener);

    /**
     * 订阅数据
     */
    void subscribe(List<Subscription> subscriptions);

    /**
     * 查询设备所有数据点的最新数据
     *
     * @param tableId  表标识
     * @param deviceId 设备编号
     */
    DeviceLatestData queryLatest(String tableId, String deviceId);

    /**
     * 批量查询设备的数据点的最新数据
     *
     * @param subscriptions 设备列表
     */
    List<DeviceLatestData> queryLatest(List<Subscription> subscriptions);
    
    /**
     * 清空所有订阅
     */
    void clearSubscriptions();

    class Subscription {
        public final String tableId;
        public final String deviceId;

        Subscription(String tableId, String deviceId) {
            this.tableId = tableId;
            this.deviceId = deviceId;
        }

        public static Subscription create(String tableId, String deviceId) {
            if (!StringUtils.hasText(tableId) || !StringUtils.hasText(deviceId)) {
                throw new IllegalArgumentException("无效的订阅, 表标识和设备编号不能为空");
            }

            return new Subscription(tableId, deviceId);
        }
    }
}
