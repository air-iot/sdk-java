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

package io.github.airiot.sdk.flow.plugin.execute;

import io.github.airiot.sdk.flow.plugin.FlowPluginException;

import java.util.Map;

/**
 * 执行结果
 * <br>
 * 通常用于执行成功时的返回结果, 如果执行失败通过抛出 {@link FlowPluginException} 异常来返回错误信息
 */
public class FlowTaskResult {

    /**
     * 说明信息
     */
    private final String message;
    /**
     * 详细信息
     */
    private final String details;
    /**
     * 返回的数据. 该数据不能为基本数据类型, 必须为对象或 {@link Map <String,Object>}
     * <br>
     * 该数据为节点的输出结果, 以供流程中的后续节点使用(暂未实现)
     */
    private final Object data;

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public Object getData() {
        return data;
    }

    public FlowTaskResult(String message, String details, Object data) {
        this.message = message;
        this.details = details;
        this.data = data;
    }

    public static FlowTaskResult success() {
        return new FlowTaskResult("success", "", null);
    }

    public static FlowTaskResult withData(Object data) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be nul");
        }
        return new FlowTaskResult("success", "", data);
    }

    public static FlowTaskResult withoutData(String message, String details) {
        return new FlowTaskResult(message, details, null);
    }

    @Override
    public String toString() {
        return "FlowTaskResult{" +
                "message='" + message + '\'' +
                ", details='" + details + '\'' +
                ", data=" + data +
                '}';
    }
}
