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

        public WithContext project(String projectId) {
            this.context.setProjectId(projectId);
            return this;
        }

        public WithContext module(String module) {
            this.context.setModule(module);
            return this;
        }

        public WithContext data(Object data) {
            this.context.setData(data);
            return this;
        }

        public WithContext data(String key, Object value) {
            this.context.setData(key, value);
            return this;
        }

        public WithContext key(String value) {
            this.context.setKey(value);
            return this;
        }

        public org.slf4j.Logger getLogger(Class<?> clazz) {
            return getLogger(clazz.getName());
        }

        public org.slf4j.Logger getLogger(String name) {
            if (this.context.getModule() == null) {
                throw new IllegalArgumentException("未设置日志的模块名");
            }

            if (LoggerContexts.getMode() == Mode.DEV) {
                return org.slf4j.LoggerFactory.getLogger(name);
            }

            String module = this.context.getModule();
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(String.format("%s#%s", name, module));
            return new JsonLoggerWithContext(logger, this.context, LoggerContexts.getMode() != Mode.DEV);
        }
    }
}
