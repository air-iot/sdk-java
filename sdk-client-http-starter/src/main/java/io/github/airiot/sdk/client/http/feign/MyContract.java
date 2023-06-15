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

package io.github.airiot.sdk.client.http.feign;

import feign.Contract;
import feign.MethodMetadata;
import feign.Param;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 如果方法的返回值为 {@link ResponseDTO} 类型, 则将泛型参数作为返回值类型
 */
public class MyContract extends Contract.Default {

    private final QueryParamExpander queryParamExpander = new QueryParamExpander();

    public MyContract() {
        super();
    }

    @Override
    protected MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
        MethodMetadata metadata = super.parseAndValidateMetadata(targetType, method);
        Type returnType = metadata.returnType();

        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];

            // 如果参数类型为 Query, 则使用 QueryParamExpander
            if (Query.class.isAssignableFrom(parameterType)) {
                Map<Integer, Param.Expander> expanders = metadata.indexToExpander();
                if (expanders == null) {
                    expanders = new HashMap<>();
                }
                expanders.put(i, queryParamExpander);
                metadata.indexToExpander(expanders);
            }
        }

        if (returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                return metadata;
            }


            if (!ResponseDTO.class.isAssignableFrom((Class<?>) rawType)) {
                return metadata;
            }

            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length != 1) {
                return metadata;
            }

            Type rType = typeArguments[0];
            // 如果返回值为 ResponseDTO<Void> 类型, 则将返回值类型设置为 ResponseOk
            if (rType == Void.class) {
                metadata.returnType(ResponseOk.class);
            } else {
                metadata.returnType(rType);
            }
        }
        return metadata;
    }
}
