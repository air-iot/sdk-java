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

import com.google.gson.reflect.TypeToken;
import io.github.airiot.sdk.driver.data.warning.Warning;
import io.github.airiot.sdk.driver.data.warning.WarningRecovery;
import io.github.airiot.sdk.driver.data.warning.WarningSenderException;
import io.github.airiot.sdk.driver.grpc.driver.Response;
import io.github.airiot.sdk.driver.model.Event;
import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.driver.model.RunLog;
import io.github.airiot.sdk.driver.model.UpdateTableDTO;
import org.springframework.context.SmartLifecycle;

import java.util.Map;


/**
 * 驱动与平台交互接口
 * <br>
 * 主要用于驱动向平台上报采集到的数据, 运行过程中的重要事件, 日志等信息
 */
public interface DataSender extends SmartLifecycle {

    TypeToken<Map<String, Object>> MAP_TYPE_TOKEN = new TypeToken<Map<String, Object>>() {
    };

    /**
     * 上报驱动采集到的数据
     *
     * @param point 数据点
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws DataSenderException   如果上报数据时发生异常
     */
    void writePoint(Point point) throws DataSenderException;

    /**
     * 上报资产采集到的数据. 部分信息会自动填充
     *
     * @param tableId   设备所属工作表标识
     * @param deviceId  设备编号
     * @param time      数据产生或采集的时间. unix时间戳(ms)
     * @param tagValues 数据点的值. key 为 数据点的标识, value 为 {@code null}的数据点不会上报
     * @throws IllegalStateException    如果连接未建立或已断开
     * @throws IllegalArgumentException 如果设备不存在或者数据点信息不正确
     * @throws DataSenderException      如果上报数据时发生异常
     */
    void writePoint(String tableId, String deviceId, long time, Map<String, Object> tagValues) throws DataSenderException;

    /**
     * 上报资产采集到的数据. 部分信息会自动填充. 使用服务器接收到数据的时间作为数据产生时间
     *
     * @param tableId   设备所属工作表标识
     * @param deviceId  设备编号
     * @param tagValues 数据点的值. key 为 数据点的标识, value 为 {@code null}的数据点不会上报
     * @throws IllegalStateException    如果连接未建立或已断开
     * @throws IllegalArgumentException 如果设备不存在或者数据点信息不正确
     * @throws DataSenderException      如果上报数据时发生异常
     */
    default void writePoint(String tableId, String deviceId, Map<String, Object> tagValues) throws DataSenderException {
        this.writePoint(tableId, deviceId, 0L, tagValues);
    }

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
     * 上报指令执行结果日志.
     * <br>
     * 一条指令可以产生多条日志
     *
     * @param runLog 日志
     * @return 上报结果
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws RunLogSenderException 如果发送运行日志时发生异常
     */
    Response writeRunLog(RunLog runLog) throws RunLogSenderException;

    /**
     * 查询表中指定的设备信息
     *
     * @param tClass   设备信息类型
     * @param tableId  表标识
     * @param deviceId 设备编号
     * @param <T>      设备信息类型泛型
     * @return 设备信息
     * @throws QueryTableDataException 如果查询失败
     */
    <T> T findTableData(Class<T> tClass, String tableId, String deviceId) throws QueryTableDataException;

    /**
     * 查询表中指定的设备信息
     *
     * @param tClass   设备信息类型
     * @param tableId  表标识
     * @param deviceId 设备编号
     * @param <T>      设备信息类型泛型
     * @return 设备信息
     * @throws QueryTableDataException 如果查询失败
     */
    <T> T findTableData(TypeToken<T> tClass, String tableId, String deviceId) throws QueryTableDataException;

    /**
     * 查询表中指定的设备信息, 并以 Map 的结构返回
     *
     * @param tableId  表标识
     * @param deviceId 设备编号
     * @return 设备信息
     * @throws QueryTableDataException 如果查询失败
     */
    Map<String, Object> findTableData(String tableId, String deviceId) throws QueryTableDataException;

    /**
     * 更新设备信息
     *
     * @param tableDTO 设备信息
     * @return 更新结果
     * @throws IllegalArgumentException 如果参数不正确
     * @throws UpdateTableDataException 如果接口调用失败
     */
    Response updateTableData(UpdateTableDTO tableDTO) throws UpdateTableDataException;

    /**
     * 更新设备信息
     *
     * @param tableId 设备所在工作表标识
     * @param rowId   设备编号
     * @param fields  更新的字段信息
     * @return 更新结果
     */
    default Response updateTableData(String tableId, String rowId, Map<String, Object> fields) {
        return this.updateTableData(new UpdateTableDTO(tableId, rowId, fields));
    }

    /**
     * 写入 {@code debug } 级别日志
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void logDebug(String tableId, String deviceId, String msg) throws LogSenderException;

    /**
     * 写入 {@code debug } 级别日志, 发送异常时忽略
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    default void logDebugIgnoreException(String tableId, String deviceId, String msg) {
        try {
            this.logDebug(tableId, deviceId, msg);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 写入 {@code info } 级别日志
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void logInfo(String tableId, String deviceId, String msg) throws LogSenderException;

    /**
     * 写入 {@code info } 级别日志. 发送异常时忽略
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    default void logInfoIgnoreException(String tableId, String deviceId, String msg) {
        try {
            this.logInfo(tableId, deviceId, msg);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 写入 {@code warn} 级别日志
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void logWarn(String tableId, String deviceId, String msg) throws LogSenderException;

    /**
     * 写入 {@code warn} 级别日志, 发送异常时忽略
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    default void logWarnIgnoreException(String tableId, String deviceId, String msg) {
        try {
            this.logWarn(tableId, deviceId, msg);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 写入 {@code error} 级别日志
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    void logError(String tableId, String deviceId, String msg) throws LogSenderException;

    /**
     * 写入 {@code error} 级别日志, 发送异常时忽略
     * <br>
     * 该日志可以在设备调试窗口中看到
     *
     * @param tableId  设备所属工作表标识
     * @param deviceId 设备编号
     * @param msg      日志内容
     * @throws IllegalStateException 如果连接未建立或已断开
     * @throws LogSenderException    如果写日志时发生异常
     */
    default void logErrorIgnoreException(String tableId, String deviceId, String msg) {
        try {
            this.logWarn(tableId, deviceId, msg);
        } catch (Exception e) {
            // ignore
        }
    }

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

    @Override
    default int getPhase() {
        return Integer.MIN_VALUE;
    }
}
