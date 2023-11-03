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
import java.util.Optional;

/**
 * 日志上下文
 */
public class LoggerContext {

    /**
     * 该 key 可用来存储任意数据. 通常用来存储数据关联的 <b>工作表标识</b>.
     */
    public static final String KEY_ANY = "key1";
    /**
     * 关联的流程实例ID
     */
    public static final String KEY_GROUP = "group";

    public static final LoggerContext EMPTY = new LoggerContext(null);

    private LoggerContext parent;

    /**
     * 日志模式
     */
    private Mode mode = Mode.valueOf(System.getenv("LOGGING_MODE"), Mode.PRODUCT);

    private int level;

    /**
     * 链路追踪ID
     */
    private String traceId;
    /**
     * 跨度ID
     */
    private String spanId;

    /**
     * 项目ID
     */
    private String projectId = System.getProperty("project", "");
    /**
     * 服务名
     */
    private String service;
    /**
     * 模块名
     */
    private String module;
    /**
     * 自定义数据
     */
    private Object data;
    /**
     * 自定义关联数据
     * <br>
     * 例如: 如果想将日志关联表, 可以将表名赋值给该字段.
     */
    private final Map<String, Object> keys = new HashMap<>();

    public int getLevel() {
        return level;
    }

    void setParent(LoggerContext parent) {
        this.parent = parent;
        this.level = parent.getLevel() + 1;
    }

    public LoggerContext getParent() {
        return parent;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getProjectId() {
        String pId = null;
        if (projectId != null && !projectId.isEmpty()) {
            pId = projectId;
        } else if (parent != null) {
            pId = parent.getProjectId();
        }
        if (pId == null) {
            throw new IllegalArgumentException("未在日志上下文中找到项目ID");
        }
        return pId;
    }

    /**
     * 设置项目ID
     *
     * @param projectId 项目ID
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * 获取当前的服务实例ID.
     * <br>
     * 每次进程都会有一个独立的服务实例ID
     *
     * @return 服务实例ID
     */
    public String getService() {
        String svc = null;
        if (service != null && !service.isEmpty()) {
            svc = service;
        } else if (parent != null) {
            svc = parent.getService();
        }
        if (svc == null) {
            throw new IllegalArgumentException("未在日志上下文中找到服务名");
        }
        return svc;
    }

    /**
     * 设置服务实例ID.
     *
     * @param service 服务实例ID
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * 获取当前日志上下文绑定的模块名
     *
     * @return 模块名
     */
    public String getModule() {
        String mod = null;
        if (module != null && !module.isEmpty()) {
            mod = module;
        } else if (parent != null) {
            mod = parent.getModule();
        }
        if (mod == null) {
            throw new IllegalArgumentException("未在日志上下文中找到模块名");
        }
        return mod;
    }

    /**
     * 设置当前日志上下文绑定的模块名
     *
     * @param module 模块名
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * 获取自定义数据
     *
     * @return 自定义数据, 如果没有设置返回 {@code null}
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置自定义数据, 每次调用都会覆盖之前的数据
     * <br>
     * <b>注: 不能与 {@link #setData(String, Object)} 一起使用</b>
     *
     * @param data 自定义数据
     */
    public void setData(Object data) {
        if (this.data != null && !(this.data instanceof Map)) {
            throw new IllegalStateException("the context data has been set to a non-map object");
        }
        this.data = data;
    }

    /**
     * 设置自定义数据, 该方法可以多次调用. 最终所有的数据会组成一个 Map 对象
     * <br>
     * <b>注: 不能与 {@link #setData(Object)} 一起使用</b>
     *
     * @param key   自定义数据的 key
     * @param value 自定义数据的值
     */
    public void setData(String key, Object value) {
        if (this.data != null && !(this.data instanceof Map)) {
            throw new IllegalStateException("the context data has been set to a map object");
        }

        if (this.data != null) {
            ((Map<String, Object>) this.data).put(key, value);
        } else {
            Map<String, Object> data = new HashMap<>(3);
            data.put(key, value);
            this.data = data;
        }
    }

    /**
     * 清空已设置的自定义数据
     */
    public void clearData() {
        this.data = null;
    }

    /**
     * 获取自定义关联数据.
     * <br>
     * 会先在当前上下文中查找, 如果未找到则会在父级上下文中查找.
     *
     * @return 关联数据. 如果没找到则返回 {@link Optional#empty()}
     */
    public Optional<Object> getKey(String key) {
        Object value = this.keys.get(key);
        if (value == null && parent != null) {
            return parent.getKey(key);
        }
        return Optional.ofNullable(value);
    }

    /**
     * 获取所有自定义关联数据.
     *
     * @param recursive 是否递归获取父级上下文中的关联数据
     * @return 所有的关联数据
     */
    public Map<String, Object> getKeys(boolean recursive) {
        Map<String, Object> allKeys = new HashMap<>(this.keys);
        if (!recursive) {
            return allKeys;
        }

        LoggerContext previous = parent;
        for (int i = 0; i < LoggerContexts.MAX_LEVEL; i++) {
            if (previous == null) {
                return allKeys;
            }

            Map<String, Object> parentKeys = previous.getKeys(true);
            if (parentKeys == null || parentKeys.isEmpty()) {
                continue;
            }
            
            for (Map.Entry<String, Object> entry : parentKeys.entrySet()) {
                if (!allKeys.containsKey(entry.getKey())) {
                    allKeys.put(entry.getKey(), entry.getValue());
                }
            }

            previous = previous.parent;
        }

        return allKeys;
    }

    /**
     * 设置自定义关联数据
     *
     * @param value 关联数据
     */
    public void setKey(String value) {
        this.keys.put(KEY_ANY, value);
    }

    /**
     * 设置关联的驱动实例分组ID
     * <br>
     * <b>注: 该方法仅可用于驱动程序中</b>
     *
     * @param driverGroupId 驱动实例分组ID
     */
    public void setDriverGroup(String driverGroupId) {
        this.keys.put(KEY_GROUP, driverGroupId);
    }

    LoggerContext(LoggerContext parent) {
        this.parent = parent;
        if (parent == null) {
            this.level = 0;
        } else {
            this.level = parent.level + 1;
        }
    }
}
