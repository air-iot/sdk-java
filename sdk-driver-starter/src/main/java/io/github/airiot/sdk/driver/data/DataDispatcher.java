package io.github.airiot.sdk.driver.data;

import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.driver.model.RunLog;

public interface DataDispatcher {

    /**
     * 发送设备数据
     *
     * @param point 设备数据
     */
    default void writePoint(Point point) {
    }

    /**
     * 发送指令日志
     */
    default void writeRunLog(RunLog runLog) {
    }

    /**
     * 发送日志
     *
     * @param tableId  表标识
     * @param deviceId 设备编号
     * @param level    日志等级
     * @param msg      日志内容
     */
    default void writeLog(String tableId, String deviceId, String level, String msg) {
    }
}
