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

package io.github.airiot.sdk.client.http.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.airiot.sdk.client.context.RequestContext;
import io.github.airiot.sdk.client.exception.NonProjectException;
import io.github.airiot.sdk.client.service.Constants;
import org.springframework.util.StringUtils;

/**
 * 请求拦截器, 向请求头中添加相关信息
 * <br>
 * 1. 项目ID(当前请求不是 SPM 服务的接口时). <br>
 * 2. 添加统一请求头<br>
 */
public class RequestHeaderInterceptor implements RequestInterceptor {

    public static final RequestHeaderInterceptor INSTANCE = new RequestHeaderInterceptor();
    
    @Override
    public void apply(RequestTemplate template) {
        // 统一请求头
        template.header("Request-Type", "service");

        // 项目ID
        if (RequestContext.isTakeProject()) {
            String projectId = RequestContext.getProjectId();
            if (!StringUtils.hasText(projectId)) {
                throw new NonProjectException("请求 '" + template.path() + "' 接口失败, 请求上下文中未找到 'projectId' 信息. 请查看 cn.airiot.sdk.client.context.RequestContext");
            }
            template.header(Constants.HEADER_PROJECT, projectId);
        }
    }
}
