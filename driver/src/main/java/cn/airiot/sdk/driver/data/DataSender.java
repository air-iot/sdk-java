package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.model.Event;
import cn.airiot.sdk.driver.data.model.Point;
import cn.airiot.sdk.driver.data.model.RunLog;
import cn.airiot.sdk.driver.data.model.UpdateTableDTO;
import cn.airiot.sdk.driver.grpc.driver.Response;
import org.springframework.context.SmartLifecycle;

import java.util.Map;


/**
 * 数据传输接口
 * <br>
 * 主要用于向平台上报采集到的数据, 运行过程中的重要事件, 日志等信息
 */
public interface DataSender extends SmartLifecycle {

    /**
     * 上报资产采集到的数据
     *
     * @param point 数据点
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws DataSenderException   如果上报数据时发生异常
     */
    void writePoint(Point point) throws DataSenderException;

    /**
     * 上报资产采集到的数据. 部分信息会自动填充
     *
     * @param deviceId  设备标识
     * @param time      数据产生的时间. unix时间戳(ms)
     * @param tagValues 数据点的值
     * @throws IllegalStateException    如果连接未建立或已断开
     * @throws IllegalArgumentException 如果设备不存在或者数据点信息不正确
     * @throws DataSenderException      如果上报数据时发生异常
     */
    void writePoint(String deviceId, long time, Map<String, Object> tagValues) throws DataSenderException;

    /**
     * 发送事件
     *
     * @param event 事件信息
     * @return 事件发送结果
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws EventSenderException  如果发送事件时发生异常
     */
    Response writeEvent(Event event) throws EventSenderException;

    /**
     * 上报指令执行结果日志
     *
     * @param runLog 日志
     * @return 上报结果
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws RunLogSenderException 如果发送运行日志时发生异常
     */
    Response writeRunLog(RunLog runLog) throws RunLogSenderException;

    /**
     * 更新设备信息
     *
     * @param tableDTO 设备信息
     * @return 更新结果
     * @throws IllegalArgumentException 如果参数不正确
     * @throws UpdateTableDataException 如果接口调用失败
     */
    Response updateTableData(UpdateTableDTO tableDTO) throws UpdateTableDataException;

    default Response updateTableData(String tableId, String rowId, Map<String, Object> fields) {
        return this.updateTableData(new UpdateTableDTO(tableId, rowId, fields));
    }

    /**
     * 写入 {@code debug } 级别日志
     *
     * @param uid 资产ID
     * @param msg 日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void logDebug(String uid, String msg) throws LogSenderException;

    /**
     * 写入 {@code info } 级别日志
     *
     * @param uid 资产ID
     * @param msg 日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void logInfo(String uid, String msg) throws LogSenderException;

    /**
     * 写入 {@code warn} 级别日志
     *
     * @param uid 资产ID
     * @param msg 日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void logWarn(String uid, String msg) throws LogSenderException;

    /**
     * 写入 {@code error} 级别日志
     *
     * @param uid 资产ID
     * @param msg 日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void logError(String uid, String msg) throws LogSenderException;

    @Override
    default int getPhase() {
        return Integer.MIN_VALUE;
    }
}
