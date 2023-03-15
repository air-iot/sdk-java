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

/**
 * 日志发送异常
 */
public class LogSenderException extends RuntimeException {

    /**
     * 设备所属工作表标识
     */
    private final String tableId;

    /**
     * 设备ID
     */
    private final String deviceId;
    /**
     * 日志等级
     */
    private final String level;
    /**
     * 日志内容
     */
    private final String logMessage;

    public String getTableId() {
        return tableId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getLevel() {
        return level;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public LogSenderException(String tableId, String deviceId, String level, String logMessage, String message) {
        super(String.format("send device log failed, reason: %s. Table = %s, Device = %s, Level = %s, Message = %s",
                message, tableId, deviceId, level, logMessage));
        this.tableId = tableId;
        this.deviceId = deviceId;
        this.level = level;
        this.logMessage = logMessage;
    }

    public LogSenderException(String tableId, String deviceId, String level, String logMessage, Throwable cause) {
        super(String.format("send device log failed, reason: %s. Table = %s, Device = %s, Level = %s, Message = %s",
                cause.getMessage(), tableId, deviceId, level, logMessage), cause);
        this.tableId = tableId;
        this.deviceId = deviceId;
        this.level = level;
        this.logMessage = logMessage;
    }
}
