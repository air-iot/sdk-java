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

package io.github.airiot.sdk.algorithm.annotation;

import io.github.airiot.sdk.algorithm.AlgorithmApp;
import io.github.airiot.sdk.algorithm.AlgorithmFunctionDefinition;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnnotationUtils {

    /**
     * 扫描算法函数
     *
     * @param target 目标类型
     * @return 算法函数定义列表
     * @throws IllegalArgumentException 如果目标对象不是 AlgorithmApp 实现类型或算法函数定义不正确或重复定义
     * @throws NullPointerException     如果目标对象为 null
     */
    public static Map<String, AlgorithmFunctionDefinition> scanFunctions(Object target) {
        if (target == null) {
            throw new NullPointerException();
        }

        if (!(target instanceof AlgorithmApp)) {
            throw new IllegalArgumentException("the target object must be an instance of AlgorithmApp");
        }

        Method[] methods = target.getClass().getDeclaredMethods();
        if (methods.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, AlgorithmFunctionDefinition> functions = new HashMap<>();

        for (Method method : methods) {
            if (!method.isAnnotationPresent(AlgorithmFunction.class)) {
                continue;
            }

            Parameter[] parameters = method.getParameters();
            if (parameters.length < 1) {
                throw new IllegalArgumentException("the algorithm function '" + method.getName() + "' must have at least 1 parameter, 'projectId'");
            }

            if (!parameters[0].getType().equals(String.class)) {
                throw new IllegalArgumentException("the first parameter of algorithm function '" + method.getName() + "' must be 'String projectId'");
            }

            AlgorithmFunction fn = method.getAnnotation(AlgorithmFunction.class);
            String fnName = fn.value();

            if (!StringUtils.hasText(fnName)) {
                throw new IllegalArgumentException("the algorithm function '" + method.getName() + "' must have a name");
            }
            if (functions.containsKey(fnName)) {
                throw new IllegalArgumentException("the algorithm function '" + fnName + "' has already registered by function '" +
                        functions.get(fnName).getCallMethod().getName() + "'");
            }

            // 如果只有1个参数, 则算法函数接收参数类型为 Void
            Type requestType = parameters.length > 1 ? parameters[1].getType() : Void.class;
            
            functions.put(fnName, new AlgorithmFunctionDefinition(
                    fnName, requestType, target, parameters.length > 1, method
            ));
        }

        return functions;
    }
}
