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

package io.github.airiot.sdk.algorithm;

/**
 * 算法执行结果
 */
public class Response {

    /**
     * 响应码
     */
    private final int code;
    /**
     * 错误信息
     */
    private final String error;
    /**
     * 执行结果
     */
    private final Object result;

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public Object getResult() {
        return result;
    }

    public Response(int code, String error, Object result) {
        this.code = code;
        this.error = error;
        this.result = result;
    }

    public Response(int code, String error) {
        this.code = code;
        this.error = error;
        this.result = null;
    }

    @Override
    public String toString() {
        return "RunResponse{" +
                "code=" + code +
                ", error='" + error + '\'' +
                ", result=" + result +
                '}';
    }
}
