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

package io.github.airiot.sdk.client.builder;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BuilderUtils {

    private static final Map<String, LambdaCache> LAMBDA_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<String>> CLASS_COLUMNS = new ConcurrentHashMap<>();

    public static <T> List<String> getColumns(Class<T> tClass) {
        if (tClass == null) {
            throw new NullPointerException();
        }

        return CLASS_COLUMNS.computeIfAbsent(tClass, tc -> {
            Field[] fields = tClass.getDeclaredFields();
            if (fields.length == 0) {
                return Collections.emptyList();
            }

            List<String> columns = new ArrayList<>(fields.length);
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                // 如果带有 transient 或 static 修饰符则忽略
                if ((modifiers & Modifier.TRANSIENT) != 0 || (modifiers & Modifier.STATIC) != 0) {
                    continue;
                }

                if (field.isAnnotationPresent(io.github.airiot.sdk.client.annotation.Field.class)) {
                    columns.add(
                            field.getAnnotation(io.github.airiot.sdk.client.annotation.Field.class).value()
                    );
                    continue;
                }

                columns.add(field.getName());
            }
            return columns;
        });
    }

    public static <T> String getPropertyName(SFunction<T, ?> column) {
        try {
            Method method = column.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(column);
            String className = lambda.getImplClass();
            LambdaCache cache = LAMBDA_CACHE.computeIfAbsent(className, BuilderUtils::makeCache);
            return cache.getPropertyName(lambda);
        } catch (Throwable e) {
            throw new IllegalStateException("不支持 SFunction 方式获取字段名", e);
        }
    }

    public static LambdaCache makeCache(String className) {
        String javaClassName = className.replaceAll("/", ".");
        try {
            Class<?> tClass = BuilderUtils.class.getClassLoader().loadClass(javaClassName);
            return new LambdaCache(tClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("加载类失败: " + javaClassName, e);
        }
    }

    static class LambdaCache {
        private final Class<?> tClass;
        private final Map<String, String> methods = new ConcurrentHashMap<>();

        public LambdaCache(Class<?> tClass) {
            this.tClass = tClass;
        }

        public String getPropertyName(SerializedLambda lambda) {
            String methodName = lambda.getImplMethodName();
            return this.methods.computeIfAbsent(methodName, this::extractPropertyName);
        }

        private String extractPropertyName(String method) {
            String propName = method;
            if (method.startsWith("get") || method.startsWith("set")) {
                propName = method.substring(3);
            } else if (method.startsWith("is")) {
                propName = method.substring(2);
            }

            char firstChar = propName.charAt(0);
            if (firstChar >= 'A' && firstChar <= 'Z') {
                propName = propName.substring(0, 1).toLowerCase() + propName.substring(1);
            }

            try {
                Field field = tClass.getDeclaredField(propName);
                if (field.isAnnotationPresent(io.github.airiot.sdk.client.annotation.Field.class)) {
                    return field.getAnnotation(io.github.airiot.sdk.client.annotation.Field.class).value();
                }
                return field.getName();
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(String.format("根据 %s#%s 方法提取到属性 %s, 但该属性不存在", tClass.getName(), method, propName));
            }
        }
    }
}
