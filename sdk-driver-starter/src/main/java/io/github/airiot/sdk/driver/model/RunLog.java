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

package io.github.airiot.sdk.driver.model;


/**
 * 指令执行日志, 上报到平台的执令的执行日志.
 * <br>
 * 每个指令可以发送多条执行日志.
 * <br>
 * 该信息会被上报到和存储到平台, 可以在 '指令状态管理' 页面中查看
 */
public class RunLog {
    /**
     * 平台指令序号
     * <br>
     * 每次下发的指令都有唯一的序号
     */
    private String serialNo;
    /**
     * 指令执行状态
     * <br>
     * 该信息可根据自身业务进行定义
     */
    private String status;
    /**
     * 指令日志产生的时间(ms)
     */
    private long time;
    /**
     * 指令日志详细信息
     */
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
