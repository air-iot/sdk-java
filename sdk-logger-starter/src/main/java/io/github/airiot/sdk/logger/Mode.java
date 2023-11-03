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

/**
 * 日志模式
 */
public enum Mode {
    /**
     * 开发模式.
     * <br>
     * 该模式下，会使用默认的日志格式输出到控制台. 该模式输出的日志不会被平台收集.
     */
    DEV,
    /**
     * 生产模式.
     * <br>
     * 该模式下, 会用使用平台预定义好的 JSON 格式输出到控制台并被平台收集.
     */
    PRODUCT;

    public static Mode valueOf(String name, Mode defaultMode) {
        if (name == null || name.isEmpty()) {
            return defaultMode;
        }
        
        if ("DEV".equalsIgnoreCase(name.trim())) {
            return Mode.DEV;
        }
        return Mode.PRODUCT;
    }
}
