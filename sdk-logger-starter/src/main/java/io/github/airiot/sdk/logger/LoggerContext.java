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
     * 日志关联的工作表标识的 key.
     * <br>
     * 例如: 执行指令时, 可以将工作表标识设置到该字段, 以便在日志中关联工作表.
     */
    public static final String TABLE_KEY = "table";
    /**
     * 日志关联的设备ID
     */
    public static final String DEVICE_KEY = "tableData";
    /**
     * 关联的驱动例ID
     */
    public static final String DRIVER_GROUP_KEY = "group";
    /**
     * 关联的流程ID
     */
    public static final String FLOW_KEY = "flow";
    /**
     * 错误日志关注标识. 该 key 的值为 1 时, 表示该日志重要, 会在特定的窗口中显示.
     * <br>
     */
    public static final String FOCUS_KEY = "focus";
    /**
     * 建议信息标识. 提供针对本条日志中的错误的处理建议信息.
     */
    public static final String SUGGESTION_KEY = "suggest";
    /**
     * 日志详情. 通常用于存储一些调试信息和给开发人员的提示信息.
     */
    public static final String DETAIL_KEY = "detail";

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
     */
    private final Map<String, Object> refData = new HashMap<>();

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
    public Optional<Object> getRefData(String key) {
        Object value = this.refData.get(key);
        if (value == null && parent != null) {
            return parent.getRefData(key);
        }
        return Optional.ofNullable(value);
    }

    /**
     * 获取所有自定义关联数据.
     *
     * @param recursive 是否递归获取父级上下文中的关联数据
     * @return 所有的关联数据
     */
    public Map<String, Object> getRefData(boolean recursive) {
        Map<String, Object> allKeys = new HashMap<>(this.refData);
        if (!recursive) {
            return allKeys;
        }

        LoggerContext previous = parent;
        for (int i = 0; i < LoggerContexts.MAX_LEVEL; i++) {
            if (previous == null) {
                return allKeys;
            }

            Map<String, Object> parentKeys = previous.getRefData(true);
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
     * 设置关联的驱动实例分组ID
     * <br>
     * <b>注: 该方法仅可用于驱动程序中</b>
     *
     * @param driverGroupId 驱动实例分组ID
     */
    public LoggerContext withDriverGroup(String driverGroupId) {
        this.refData.put(DRIVER_GROUP_KEY, driverGroupId);
        return this;
    }

    /**
     * 设置关联的工作表标识
     *
     * @param tableId 关联的工作表标识
     */
    public LoggerContext withTable(String tableId) {
        this.refData.put(TABLE_KEY, tableId);
        return this;
    }

    /**
     * 设置关联的设备编号
     *
     * @param deviceId 设备编号
     */
    public LoggerContext withDevice(String deviceId) {
        this.refData.put(DEVICE_KEY, deviceId);
        return this;
    }

    /**
     * 设置关联的工作表标识和设备编号
     *
     * @param tableId  工作表标识
     * @param deviceId 设备编号
     */
    public LoggerContext withTableDevice(String tableId, String deviceId) {
        this.refData.put(TABLE_KEY, tableId);
        this.refData.put(DEVICE_KEY, deviceId);
        return this;
    }

    /**
     * 设置关联的流程ID
     *
     * @param flowId 流程ID
     */
    public LoggerContext withFlow(String flowId) {
        this.refData.put(FLOW_KEY, flowId);
        return this;
    }
    
    /**
     * 设置关注标识
     *
     * @param value 标识的值
     */
    public LoggerContext withFocus(int value) {
        this.refData.put(FOCUS_KEY, value);
        return this;
    }

    /**
     * 设置默认关注标识. 关注标识的值为 1
     */
    public LoggerContext withFocus() {
        return this.withFocus(1);
    }

    /**
     * 设置建议信息
     *
     * @param value 建议信息
     */
    public LoggerContext withSuggestion(String value) {
        this.refData.put(SUGGESTION_KEY, value);
        return this;
    }

    /**
     * 设置日志详情
     *
     * @param detail 日志详情
     */
    public LoggerContext withDetail(String detail) {
        this.refData.put(DETAIL_KEY, detail);
        return this;
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
