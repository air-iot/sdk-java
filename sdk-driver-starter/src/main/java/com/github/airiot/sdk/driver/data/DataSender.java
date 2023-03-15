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

package com.github.airiot.sdk.driver.data;

import com.github.airiot.sdk.driver.grpc.driver.Response;
import com.github.airiot.sdk.driver.model.Event;
import com.github.airiot.sdk.driver.model.Point;
import com.github.airiot.sdk.driver.model.RunLog;
import com.github.airiot.sdk.driver.model.UpdateTableDTO;
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
     * @param tableId   设备所属工作表标识
     * @param deviceId  设备编号
     * @param time      数据产生的时间. unix时间戳(ms)
     * @param tagValues 数据点的值. value 为 {@code null}的数据点不会上报
     * @throws IllegalStateException    如果连接未建立或已断开
     * @throws IllegalArgumentException 如果设备不存在或者数据点信息不正确
     * @throws DataSenderException      如果上报数据时发生异常
     */
    void writePoint(String tableId, String deviceId, long time, Map<String, Object> tagValues) throws DataSenderException;

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

    @Override
    default int getPhase() {
        return Integer.MIN_VALUE;
    }
}
