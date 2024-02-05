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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerFactory {

    /**
     * logger 缓存
     */
    private static final Map<String, JsonLogger> LOGGER_CACHE = new ConcurrentHashMap<>();

    public static WithContext withContext() {
        return new WithContext();
    }

    public static org.slf4j.Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static org.slf4j.Logger getLogger(String name) {
        if (LoggerContexts.isDevMode()) {
            return org.slf4j.LoggerFactory.getLogger(name);
        }
        return LOGGER_CACHE.computeIfAbsent(name, loggerName -> {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(loggerName);
            return new JsonLogger((ch.qos.logback.classic.Logger) logger, true);
        });
    }

    public static class WithContext {

        private final LoggerContext context;

        WithContext() {
            this.context = LoggerContexts.createContext();
        }

        /**
         * 设置日志所属的项目ID
         *
         * @param projectId 项目ID
         */
        public WithContext project(String projectId) {
            this.context.setProjectId(projectId);
            return this;
        }

        /**
         * 设置该日志所属的模块名
         *
         * @param module 模块名
         */
        public WithContext module(String module) {
            this.context.setModule(module);
            return this;
        }

        /**
         * 设置自定义数据. 每次调用都会覆盖之前的数据
         * <br>
         *
         * <b>注: 不能与 {@link #data(String, Object)} 一起使用</b>
         *
         * @param data 自定义数据
         */
        public WithContext data(Object data) {
            this.context.setData(data);
            return this;
        }

        /**
         * 设置自定义数据. 该方法可以多次使用, 所有的数据会组成一个 Map 对象
         * <br>
         *
         * <b>注: 该方法不能与 {@link #data(Object)} 一起使用</b>
         *
         * @param key   自定义数据的 key
         * @param value 自定义数据的值
         */
        public WithContext data(String key, Object value) {
            this.context.setData(key, value);
            return this;
        }

        /**
         * 设置日志关联的表标识
         *
         * @param tableId 表标识
         */
        public WithContext table(String tableId) {
            this.context.withTable(tableId);
            return this;
        }

        /**
         * 设置日志关联的设备编号
         *
         * @param deviceId 设备编号
         */
        public WithContext device(String deviceId) {
            this.context.withDevice(deviceId);
            return this;
        }

        /**
         * 设置日志关联的流程定义ID
         *
         * @param flowId 流程定义ID
         */
        public WithContext flow(String flowId) {
            this.context.withFlow(flowId);
            return this;
        }

        /**
         * 设置日志关注标识. 设置为关注后, 该日志可以在平台中的指定位置查看
         */
        public WithContext force() {
            this.context.withFocus();
            return this;
        }

        /**
         * 设置日志关注标识. 设置为关注后, 该日志可以在平台中的指定位置查看
         * <br>
         * <b>注: 目前只支持 1.</b>
         *
         * @param forceValue 关注标识的值
         */
        public WithContext force(int forceValue) {
            this.context.withFocus(forceValue);
            return this;
        }

        /**
         * 设置驱动实例组ID
         *
         * @param driverGroupId 驱动实例组ID
         */
        public WithContext group(String driverGroupId) {
            this.context.withDriverGroup(driverGroupId);
            return this;
        }

        public org.slf4j.Logger getStaticLogger(Class<?> clazz) {
            return getStaticLogger(clazz.getName());
        }

        public org.slf4j.Logger getStaticLogger(String name) {
            if (this.context.getModule() == null) {
                throw new IllegalArgumentException("未设置日志的模块名");
            }

            if (LoggerContexts.getMode() == Mode.DEV) {
                return org.slf4j.LoggerFactory.getLogger(name);
            }

            String module = this.context.getModule();
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(String.format("%s#%s#static", name, module));
            return new JsonLoggerWithContext(logger, this.context, LoggerContexts.getMode() != Mode.DEV);
        }

        public org.slf4j.Logger getDynamicLogger(Class<?> clazz) {
            return this.getDynamicLogger(clazz.getName());
        }

        public org.slf4j.Logger getDynamicLogger(String name) {
            if (LoggerContexts.getMode() == Mode.DEV) {
                return org.slf4j.LoggerFactory.getLogger(name);
            }

            String module = this.context.getModule();
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(String.format("%s#%s#dynamic", name, module));
            return new JsonLoggerWithDynamicContext(logger, this.context, LoggerContexts.getMode() != Mode.DEV);
        }
    }
}
