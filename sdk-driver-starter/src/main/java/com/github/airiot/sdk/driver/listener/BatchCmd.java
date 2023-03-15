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


import java.util.List;

/**
 * 对多个设备下发指令
 *
 * @param <Command> 命令类型
 */
public class BatchCmd<Command> {
    /**
     * 请求标识
     */
    private final String requestId;
    /**
     * 模型ID
     */
    private final String modelId;
    /**
     * 设备ID列表
     */
    private final List<String> deviceIds;
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

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public Command getCommand() {
        return command;
    }

    public BatchCmd(String requestId, String modelId, List<String> deviceIds, String serialNo, Command command) {
        this.requestId = requestId;
        this.modelId = modelId;
        this.deviceIds = deviceIds;
        this.serialNo = serialNo;
        this.command = command;
    }

    @Override
    public String toString() {
        return "BatchCmd{" +
                "requestId='" + requestId + '\'' +
                ", modelId='" + modelId + '\'' +
                ", deviceIds=" + deviceIds +
                ", serialNo='" + serialNo + '\'' +
                ", command=" + command +
                '}';
    }
}
