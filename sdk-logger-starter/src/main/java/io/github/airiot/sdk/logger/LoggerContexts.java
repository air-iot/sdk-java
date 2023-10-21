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

package io.github.airiot.sdk.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LoggerContexts {

    /**
     * 根日志上下文
     */
    private static final LoggerContext ROOT_CONTEXT = new LoggerContext(null);

    protected static final InheritableThreadLocal<Stack<LoggerContext>> CONTEXT = new InheritableThreadLocal<>();

    /**
     * 设置日志模式为开发模式. 即使用默认的日志格式输出日志, 不使用 JSON 格式.
     */
    public static void dev() {
        ROOT_CONTEXT.setMode(Mode.DEV);
    }

    public static Mode getMode() {
        return ROOT_CONTEXT.getMode();
    }

    /**
     * 判断当前是否为 DEV 模式
     */
    public static boolean isDevMode() {
        return ROOT_CONTEXT.getMode() == Mode.DEV;
    }

    /**
     * 设置默认的项目ID
     *
     * @param projectId 项目ID
     */
    public static void setDefaultProjectId(String projectId) {
        ROOT_CONTEXT.setProjectId(projectId);
    }

    /**
     * 设置默认的服务名
     *
     * @param service 服务名
     */
    public static void setDefaultService(String service) {
        ROOT_CONTEXT.setService(service);
    }

    /**
     * 判断当前日志上下文是否为空
     *
     * @return 如果没有初始化日志上下文, 则返回 true, 否则返回 false
     */
    public static boolean isEmpty() {
        return CONTEXT.get() == null || CONTEXT.get().isEmpty();
    }

    static LoggerContext getTopContext() {
        Stack<LoggerContext> stack = CONTEXT.get();
        if (stack == null) {
            stack = new Stack<>();
            stack.push(ROOT_CONTEXT);
            CONTEXT.set(stack);
        }
        return stack.peek();
    }
    
    /**
     * 获取根上下文
     */
    public static LoggerContext getRootContext() {
        return ROOT_CONTEXT;
    }

    /**
     * 获取栈顶的日志上下文
     *
     * @return 日志上下文
     */
    public static LoggerContext getContext() {
        return getTopContext();
    }

    /**
     * 创建一个新的日志上下文, 并以 ROOT_CONTEXT 作为父上下文.
     *
     * @return 新上下文
     */
    public static LoggerContext createContext() {
        return new LoggerContext(ROOT_CONTEXT);
    }

    /**
     * 初始化新的日志上下文, 初始化后的日志上下文包含 ROOT_CONTEXT 和一个新的日志上下文.
     *
     * @return 新的日志上下文
     */
    public static LoggerContext initial() {
        Stack<LoggerContext> stack = CONTEXT.get();
        if (stack == null) {
            stack = new Stack<>();
            CONTEXT.set(stack);
        } else {
            stack.clear();
        }

        LoggerContext context = new LoggerContext(ROOT_CONTEXT);
        stack.push(ROOT_CONTEXT);
        stack.push(context);

        return context;
    }

    /**
     * 销毁当前线程的日志上下文
     */
    public static void destroy() {
        Stack<LoggerContext> stack = CONTEXT.get();
        if (stack != null) {
            stack.clear();
        }
    }

    /**
     * 创建一个新的日志上下文. 如果当前线程已经有了日志上下文, 则会将当栈顶的日志上下文作为新创建的日志上下文的父上下文.
     * <b>注: 在日志输出完成后, 调用 {@link #pop()} 方法弹出该元素, 否则可能产生内存泄漏</b>
     */
    public static LoggerContext push() {
        Stack<LoggerContext> stack = CONTEXT.get();
        if (stack == null) {
            stack = new Stack<>();
            stack.push(ROOT_CONTEXT);
            CONTEXT.set(stack);
        }

        LoggerContext context = new LoggerContext(stack.peek());
        stack.push(context);

        if (stack.size() > 10) {
            System.out.println("[WARN] 当前线程的日志上下文栈中的元素个数已经超过 10 个, 可能存在内存泄漏. 请检查代码是否正确使用了 LoggerContexts.push() 和 LoggerContexts.pop() 方法.");
        }

        return context;
    }

    /**
     * 弹出当前线程的日志上下文栈顶的元素
     *
     * @return 如果当前线程的日志上下文栈为空, 则返回 null, 否则返回栈顶的元素
     */
    public static LoggerContext pop() {
        Stack<LoggerContext> stack = CONTEXT.get();
        if (stack == null) {
            return null;
        }
        return stack.pop();
    }

    /**
     * 设置项目ID
     *
     * @param projectId 项目ID
     * @throws IllegalArgumentException 如果项目ID为 null 或空字符串
     */
    public static void project(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("日志上下文中的 projectId 不能为空");
        }

        getTopContext().setProjectId(projectId.trim());
    }

    /**
     * 设置模块名
     *
     * @param module 模块名
     */
    public static void module(String module) {
        if (module == null || module.isEmpty()) {
            throw new IllegalArgumentException("日志上下文中的 module 不能为空");
        }

        getTopContext().setModule(module.trim());
    }

    /**
     * 设置自定义数据. 如果已经设置过自定义数据, 则会覆盖之前的数据.
     * <br>
     * <b>注: 该方法不能与 {@link #data(String, Object)} 同时使用.</b>
     *
     * @param data 自定义数据
     */
    public static void data(Object data) {
        LoggerContext context = getTopContext();
        Object contextData = context.getData();
        if (contextData != null && !(contextData instanceof Map)) {
            throw new IllegalStateException("the context data has been set to a non-map object");
        }

        getTopContext().setData(data);
    }

    /**
     * 设置自定义数据. 可多次使用, 每次调用该方法都会将数据添加到之前的数据中.
     * <br>
     * <b>注: 该方法不能与 {@link #data(Object)} 同时使用.</b>
     *
     * @param key   数据项的 key
     * @param value 数据项目的 value
     */
    public static void data(String key, Object value) {
        LoggerContext context = getTopContext();
        Object contextData = context.getData();
        if (contextData == null) {
            Map<String, Object> data = new HashMap<>(3);
            data.put(key, value);
            context.setData(data);
        } else if (contextData instanceof Map) {
            ((Map<String, Object>) contextData).put(key, value);
        } else {
            throw new IllegalStateException("the context data has been set to a map object");
        }
    }

    /**
     * 清除自定义数据
     */
    public static void clearData() {
        getTopContext().setData(null);
    }

    /**
     * 设置自定义关联数据
     *
     * @param value 关联数据
     */
    public static void key(String value) {
        getTopContext().setKey(value);
    }
}
