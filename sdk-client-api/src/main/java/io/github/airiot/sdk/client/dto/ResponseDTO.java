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

package io.github.airiot.sdk.client.dto;

import io.github.airiot.sdk.client.exception.RequestFailedException;

import java.util.function.Supplier;

/**
 * 统一请求响应数据类
 *
 * @param <T> 响应数据的类型
 */
public class ResponseDTO<T> {

    /**
     * 请求是否成功标识. 如果为 {@code true} 表明请求成功, 否则请求失败
     */
    private boolean success;
    /**
     * 请求状态码
     */
    private int code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 详细信息
     */
    private String detail;
    /**
     * 字段级别的错误信息
     */
    private String field;
    /**
     * 总记录数
     * <br>
     * 当查询请求设置了 {@code cn.airiot.sdk.client.builder.Query#withCount} 为 {@code true} 时, 该字段为匹配的记录数量.
     */
    private long count;
    /**
     * 响应数据
     */
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public long getCount() {
        return count;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public T getData() {
        return data;
    }

    public ResponseDTO() {
    }

    public ResponseDTO(boolean success, int code, String message, String detail, T data) {
        this.success = success;
        this.count = 0;
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.data = data;
    }

    public ResponseDTO(boolean success, long count, int code, String message, String detail, T data) {
        this.success = success;
        this.count = count;
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.data = data;
    }

    public ResponseDTO(boolean success, long count, int code, String message, String detail, String field, T data) {
        this.success = success;
        this.count = count;
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.field = field;
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "success=" + success +
                ", count=" + count +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", detail='" + detail + '\'' +
                ", field='" + field + '\'' +
                ", data=" + data +
                ", fullMessage='" + getFullMessage() + '\'' +
                ", unwrap=" + unwrap() +
                '}';
    }

    public String getFullMessage() {
        if (this.detail == null || this.detail.trim().isEmpty()) {
            return this.message;
        }
        return this.message + ". " + this.detail.trim();
    }

    /**
     * 如果请求成功, 则返回 {@link #data}, 否则抛出 {@link RequestFailedException}
     *
     * @return 请求响应数据
     * @throws RequestFailedException 请求失败异常
     */
    public T unwrap() throws RequestFailedException {
        if (isSuccess()) {
            return this.data;
        }
        throw new RequestFailedException(this.code, this.message, this.detail);
    }

    /**
     * 如果请求成功, 则返回 {@link #data}, 否则抛出 {@link E}
     *
     * @param supplier 异常信息
     * @param <E>      自定义异常
     * @return 请求响应数据
     * @throws E 异常类型
     */
    public <E extends RequestFailedException> T unwrap(Supplier<E> supplier) throws E {
        if (isSuccess()) {
            return this.data;
        }
        throw supplier.get();
    }
}
