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

package io.github.airiot.sdk.logger.suggestion;


/**
 * 带有建议信息的异常. 包装异常和建议信息, 用于日志输出时自动输出建议信息.
 */
public class SuggestionException extends Exception {

    /**
     * 建议信息
     */
    private final String suggestion;

    public String getSuggestion() {
        return suggestion;
    }

    public SuggestionException(Throwable cause, String suggestion) {
        super(cause);
        this.suggestion = suggestion;
    }
}
