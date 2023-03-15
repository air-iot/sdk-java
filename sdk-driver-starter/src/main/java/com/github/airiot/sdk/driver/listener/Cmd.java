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

package com.github.airiot.sdk.driver.listener;


/**
 * 对单个设备下发指令
 *
 * @param <Command> 命令类型
 */
public class Cmd<Command> {

    /**
     * 请求标识
     */
    private final String requestId;
    /**
     * 模型ID
     */
    private final String modelId;
    /**
     * 设备ID
     */
    private final String deviceId;
    /**
     * 序号
     */
    private final String serialNo;
    /**
     * 命令
     */
    private final Command command;

    public String getRequestId() {
        return requestId;
    }

    public String getModelId() {
        return modelId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public Command getCommand() {
        return command;
    }

    public Cmd(String requestId, String modelId, String deviceId, String serialNo, Command command) {
        this.requestId = requestId;
        this.modelId = modelId;
        this.deviceId = deviceId;
        this.serialNo = serialNo;
        this.command = command;
    }

    @Override
    public String toString() {
        return "Cmd{" +
                "requestId='" + requestId + '\'' +
                ", modelId='" + modelId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", serialNo='" + serialNo + '\'' +
                ", command=" + command +
                '}';
    }
}
