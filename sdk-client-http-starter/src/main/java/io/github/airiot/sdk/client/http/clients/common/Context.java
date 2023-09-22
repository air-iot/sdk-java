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

package io.github.airiot.sdk.client.http.clients.common;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 请求上下文. 用于定义本次请求的一些配置及参数信息
 */
public class Context {
    
    public static final Context EMPTY = new Context(null, null, null, null);

    /**
     * 项目ID, 如果不为空则添加到请求头中
     */
    private final String projectId;
    /**
     * 自定义 token, 如果不定义则自动获取
     */
    private final String token;
    /**
     * 自定义请求头
     */
    private final Map<String, String> headers;
    /**
     * 请求超时时间
     */
    private final Duration timeout;


    public String getProjectId() {
        return projectId;
    }

    public String getToken() {
        return token;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Context(String projectId, String token, Map<String, String> headers, Duration timeout) {
        this.projectId = projectId;
        this.token = token;
        this.headers = headers;
        this.timeout = timeout;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String projectId;
        private String token;
        private Map<String, String> headers;
        private Duration timeout;

        public Context build() {
            return new Context(this.projectId, this.token, this.headers, this.timeout);
        }

        /**
         * 设置请求数据所在的项目ID
         *
         * @param projectId 项目ID
         * @throws IllegalArgumentException 如果 projectId 为空
         */
        public Builder project(String projectId) {
            if (!StringUtils.hasText(projectId)) {
                throw new IllegalArgumentException("cannot set an empty project id");
            }
            this.projectId = projectId;
            return this;
        }

        /**
         * 设置自定义的 token
         *
         * @param token token
         * @throws IllegalArgumentException 如果 token 为空
         */
        public Builder token(String token) {
            if (!StringUtils.hasText(token)) {
                throw new IllegalArgumentException("cannot set an empty token");
            }
            this.token = token;
            return this;
        }

        /**
         * 设置自定义的请求头
         *
         * @param headers 请求头
         */
        public Builder headers(Map<String, String> headers) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 设置自定义的请求头
         *
         * @param key   请求头的 key
         * @param value 请求头的 value
         */
        public Builder header(String key, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(key, value);
            return this;
        }

        /**
         * 设置请求超时时间
         *
         * @param timeout 超时时间
         * @param unit    时间单位
         * @throws IllegalArgumentException 如果 timeout 小于 0
         */
        public Builder connectTimeout(long timeout, TimeUnit unit) {
            if (timeout < 0) {
                throw new IllegalArgumentException("connectTimeout must be greater than or equal to 0");
            }
            this.timeout = Duration.ofMillis(unit.convert(timeout, TimeUnit.MILLISECONDS));
            return this;
        }

        /**
         * 设置请求超时时间
         *
         * @param timeout 超时时间
         * @throws IllegalArgumentException 如果 connectTimeout 为 {@code null}
         */
        public Builder connectTimeout(Duration timeout) {
            Assert.notNull(timeout, "connectTimeout must not be null");
            this.timeout = timeout;
            return this;
        }
    }
}
