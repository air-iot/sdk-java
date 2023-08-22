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

package io.github.airiot.sdk.algorithm;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class AlgorithmFunctionDefinition {

    /**
     * 函数名
     */
    private final String name;

    /**
     * 该函数的请求参数类型
     */
    private final Type requestType;

    /**
     * 函数所属对象
     */
    private final Object target;
    /**
     * 算法函数是否有请求参数定义. 即除了 {@code projectId} 外, 是否还有第 2 个参数接收请求参数
     */
    private final boolean hasParams;

    /**
     * 执行函数
     */
    private final Method callMethod;

    public String getName() {
        return name;
    }

    public Type getRequestType() {
        return requestType;
    }

    public Object getTarget() {
        return target;
    }

    public boolean isHasParams() {
        return hasParams;
    }

    public Method getCallMethod() {
        return callMethod;
    }

    public AlgorithmFunctionDefinition(String name, Type requestType, Object target, boolean hasParams, Method callMethod) {
        this.name = name;
        this.requestType = requestType;
        this.target = target;
        this.hasParams = hasParams;
        this.callMethod = callMethod;
    }
}
