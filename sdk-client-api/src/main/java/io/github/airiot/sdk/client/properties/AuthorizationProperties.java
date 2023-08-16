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

package io.github.airiot.sdk.client.properties;


import io.github.airiot.sdk.client.context.RequestContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 平台认证配置
 */
@ConfigurationProperties(prefix = AuthorizationProperties.PREFIX)
public class AuthorizationProperties implements InitializingBean {

    public static final String PREFIX = "airiot.client.authorization";

    /**
     * 认证类型
     */
    private Type type = Type.PROJECT;
    /**
     * 认证信息
     */
    private String appKey;
    private String appSecret;
    /**
     * 项目ID. 默认为 {@code default}
     * <br>
     * 如果 {@link #type} 为 {@link Type#PROJECT} 时不能为空.
     * <br>
     * 除了配置文件之外, 也可以通过启动参数 {@code --project} 动态设置
     */
    @Value("${project:default}")
    private String projectId = "";

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Type.PROJECT.equals(this.type) && !StringUtils.hasText(this.projectId)) {
            throw new IllegalArgumentException("设置 airiot.client.authorization.type=PROJECT 时, 必须设置 airiot.client.authorization.project-id");
        }
        if (StringUtils.hasText(this.projectId)) {
            RequestContext.setDefaultProjectId(this.projectId);
        }

        if (!StringUtils.hasText(this.appKey) || !StringUtils.hasText(this.appSecret)) {
            throw new IllegalArgumentException("未设置 appKey 或 appSecret");
        }
    }

    public enum Type {
        /**
         * 项目级授权
         */
        PROJECT,
        /**
         * 租户级授权
         */
        TENANT;
    }
}
