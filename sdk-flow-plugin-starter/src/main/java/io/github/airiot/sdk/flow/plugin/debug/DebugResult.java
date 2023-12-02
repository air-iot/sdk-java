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

package io.github.airiot.sdk.flow.plugin.debug;


import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 调试输出结果, 即调试返回值, 以供流程中的后续节点使用
 */
public class DebugResult {

    /**
     * 调试是否执行成功
     */
    @SerializedName("status")
    private final boolean success;
    /**
     * 失败原因
     */
    @SerializedName("info")
    private final String reason;
    /**
     * 失败的详细说明
     */
    @SerializedName("detail")
    private final String detail;

    /**
     * 调试日志
     */
    private final List<DebugLog> logs;
    /**
     * 输出结果.
     * <br>
     * 注: 输出结果必须是一个可以序列化为 JSON 对象的数据. 例如: 自定义类型或 Map 等
     */
    private final Object value;

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }

    public String getDetail() {
        return detail;
    }

    public List<DebugLog> getLogs() {
        return logs;
    }

    public Object getValue() {
        return value;
    }

    public DebugResult(boolean success, String reason, String detail, List<DebugLog> logs, Object value) {
        this.success = success;
        this.reason = reason;
        this.detail = detail;
        this.logs = logs;
        this.value = value;
    }

    @Override
    public String toString() {
        return "DebugResult{" +
                "success=" + success +
                ", reason='" + reason + '\'' +
                ", detail='" + detail + '\'' +
                ", logs=" + logs +
                ", value=" + value +
                '}';
    }
}
