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

package com.github.airiot.sdk.driver.data;

import com.github.airiot.sdk.driver.model.RunLog;


/**
 * 发送运行日志失败异常
 */
public class RunLogSenderException extends RuntimeException {

    private final RunLog runLog;

    public RunLog getRunLog() {
        return runLog;
    }

    public RunLogSenderException(RunLog runLog, String message) {
        super(message);
        this.runLog = runLog;
    }

    public RunLogSenderException(RunLog runLog, String message, Throwable cause) {
        super(message, cause);
        this.runLog = runLog;
    }

    public RunLogSenderException(RunLog runLog, Throwable cause) {
        super(cause);
        this.runLog = runLog;
    }
}
