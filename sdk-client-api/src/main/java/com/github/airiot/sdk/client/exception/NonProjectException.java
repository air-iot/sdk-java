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

package com.github.airiot.sdk.client.exception;


import com.github.airiot.sdk.client.context.RequestContext;
import com.github.airiot.sdk.client.annotation.NonProject;

/**
 * 请求上下文中未找到 {@code projectId} 信息时抛出的异常.
 * <br>
 * 如果 {@link RequestContext#getProjectId()} 返回的信息为 {@code null} 或空字符串并且客户实现类或方法上没有 {@link NonProject} 注解时则抛出该异常
 */
public class NonProjectException extends PlatformException {
    public NonProjectException(String message) {
        super(message);
    }
}
