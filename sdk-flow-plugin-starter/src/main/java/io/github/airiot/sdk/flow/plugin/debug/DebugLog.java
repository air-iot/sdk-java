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

package io.github.airiot.sdk.flow.plugin.debug;

/**
 * 调试日志
 */
public class DebugLog {
    /**
     * 日志的等级
     */
    private final String level;
    /**
     * 日志的时间
     */
    private final String time;
    /**
     * 日志的内容
     */
    private final String msg;

    public String getLevel() {
        return level;
    }

    public String getTime() {
        return time;
    }

    public String getMsg() {
        return msg;
    }

    public DebugLog(String level, String time, String msg) {
        this.level = level;
        this.time = time;
        this.msg = msg;
    }
}
