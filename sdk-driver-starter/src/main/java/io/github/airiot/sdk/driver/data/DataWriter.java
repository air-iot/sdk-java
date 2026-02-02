package io.github.airiot.sdk.driver.data;


import io.github.airiot.sdk.driver.data.warning.Warning;
import io.github.airiot.sdk.driver.data.warning.WarningRecovery;
import io.github.airiot.sdk.driver.data.warning.WarningSenderException;
import io.github.airiot.sdk.driver.model.Point;

public interface DataWriter {

    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 是否正在运行
     *
     * @return 如果正在运行返回 true, 否则返回 false.
     */
    boolean isRunning();

    /**
     * 发送设备数据
     *
     * @param point 设备类型
     * @throws DataSenderException 发送失败时抛出该异常.
     */
    void writePoint(Point point) throws DataSenderException;

    /**
     * 写入日志, 发送异常时忽略
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param level    日志等级
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void writeLog(String tableId, String deviceId, String level, String msg) throws LogSenderException;


    /**
     * 发送报警信息
     *
     * @param warning 报警信息
     * @throws WarningSenderException 如果报警信息发送失败
     */
    void sendWarning(Warning warning) throws WarningSenderException;

    /**
     * 发送报警恢复信息
     *
     * @param tableId  报警设备所属工作表标识
     * @param deviceId 报警设备的编号
     * @param recovery 报警恢复信息
     * @throws WarningSenderException 如果报警恢复信息发送失败
     */
    void recoverWarning(String tableId, String deviceId, WarningRecovery recovery) throws WarningSenderException;
}
