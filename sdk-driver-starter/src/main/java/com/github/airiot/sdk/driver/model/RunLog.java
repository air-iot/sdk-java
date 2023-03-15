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

package com.github.airiot.sdk.driver.model;


import com.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;

/**
 * 指令执行日志, 驱动上报平台下发的执行执行结果
 * <br>
 *
 * @see DriverServiceGrpc#getCommandLogMethod()
 */
public class RunLog {
    private String serialNo;
    private String status;
    private long time;
    private String desc;

    public RunLog() {
    }

    public RunLog(String serialNo, String status, long time, String desc) {
        this.serialNo = serialNo;
        this.status = status;
        this.time = time;
        this.desc = desc;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "RunLog{" +
                "serialNo='" + serialNo + '\'' +
                ", status='" + status + '\'' +
                ", time=" + time +
                ", desc='" + desc + '\'' +
                '}';
    }
}
