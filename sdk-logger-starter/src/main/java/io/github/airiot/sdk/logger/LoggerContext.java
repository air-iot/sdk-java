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

/**
 * 日志上下文
 */
public class LoggerContext {

    public static final LoggerContext EMPTY = new LoggerContext(null);

    private final LoggerContext parent;

    /**
     * 日志模式
     */
    private Mode mode = Mode.valueOf(System.getenv("LOGGING_MODE"), Mode.PRODUCT);

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
    private String key;

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

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

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

    public void setService(String service) {
        this.service = service;
    }

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

    public void setModule(String module) {
        this.module = module;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        if (this.data != null && !(this.data instanceof Map)) {
            throw new IllegalStateException("the context data has been set to a non-map object");
        }
        this.data = data;
    }

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

    public void clearData() {
        this.data = null;
    }

    /**
     * 获取自定义关联数据
     *
     * @return 关联数据
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置自定义关联数据
     *
     * @param value 关联数据
     */
    public void setKey(String value) {
        this.key = value;
    }

    public LoggerContext(LoggerContext parent) {
        this.parent = parent;
    }
}
