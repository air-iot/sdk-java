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

package io.github.airiot.sdk.client.context;


import org.springframework.util.StringUtils;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 请求上下文
 */
public class RequestContext {

    private static final AtomicReference<String> DEFAULT_PROJECT_ID = new AtomicReference<>();

    public static void setDefaultProjectId(String projectId) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("cannot set default projectId to empty");
        }
        DEFAULT_PROJECT_ID.set(projectId.trim());
    }

    private final static ThreadLocal<Stack<ContextData>> RT_CONTEXT = ThreadLocal.withInitial(() -> {
        Stack<ContextData> stack = new Stack<>();
        ContextData data = new ContextData();
        data.projectId = DEFAULT_PROJECT_ID.get();
        stack.push(data);
        return stack;
    });

    public static void push() {
        ContextData data = new ContextData(RT_CONTEXT.get().firstElement());
        RT_CONTEXT.get().push(data);
    }

    public static void pop() {
        if (RT_CONTEXT.get().size() == 1) {
            throw new IllegalStateException("cannot pop the last request context");
        }

        RT_CONTEXT.get().pop();
    }

    /**
     * 设置当前请求的项目ID
     *
     * @param projectId 项目ID
     */
    public static void setProjectId(String projectId) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("projectId cannot be empty");
        }

        ContextData data = RT_CONTEXT.get().firstElement();
        data.projectId = projectId;
    }

    /**
     * 获取当前已设置的项目ID
     *
     * @return 项目ID
     */
    public static String getProjectId() {
        return RT_CONTEXT.get().firstElement().projectId;
    }

    /**
     * 清空当前请求的项目ID
     */
    public static void clearProjectId() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.projectId = null;
    }

    public static void disableAuth() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.disableAuth = true;
    }

    public static void enableAuth() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.disableAuth = false;
    }

    public static boolean isAuthEnabled() {
        return !RT_CONTEXT.get().firstElement().disableAuth;
    }

    public static void disableTakeProject() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.takeProject = false;
    }

    public static void enableTakeProject() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.takeProject = true;
    }

    public static boolean isTakeProject() {
        return RT_CONTEXT.get().firstElement().takeProject;
    }
}
