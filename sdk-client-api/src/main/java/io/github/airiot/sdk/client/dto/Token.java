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


import java.time.Duration;

/**
 * 令牌信息
 */
public class Token {

    /**
     * 令牌类型
     */
    private String tokenType;
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 令牌
     */
    private String token;
    /**
     * 当前用户ID
     */
    private String userId;
    /**
     * 令牌有效期
     */
    private Long expires;
    /**
     * 令牌到期时间
     */
    private Long expiresAt;
    /**
     * 当前用户是否为管理员
     */
    private Boolean isAdmin;

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public Long getExpires() {
        return expires;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    /**
     * 判断 token 是否即将过期, 即在指定的未来一段时间内会过期
     *
     * @param advance 即将过期的时间范围
     * @return 如果即将过期则返回 {@code true}, 否则返回 {@code false}
     */
    public boolean isExpired(Duration advance) {
        return this.expiresAt <= System.currentTimeMillis() / 1000 - advance.getSeconds();
    }

    /**
     * 判断 token是否已经过期
     *
     * @return 如果已经过期则返回 {@code true}, 否则返回 {@code false}
     */
    public boolean isExpired() {
        return this.isExpired(Duration.ofSeconds(0));
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenType='" + tokenType + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", token='" + token + '\'' +
                ", userId='" + userId + '\'' +
                ", expires=" + expires +
                ", expiresAt=" + expiresAt +
                ", isAdmin=" + isAdmin +
                ", admin=" + getAdmin() +
                ", expired=" + isExpired() +
                '}';
    }
}
