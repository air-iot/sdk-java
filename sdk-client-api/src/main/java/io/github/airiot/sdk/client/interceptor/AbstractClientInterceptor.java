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

package io.github.airiot.sdk.client.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class AbstractClientInterceptor implements PointcutAdvisor {

    private final Class<? extends Annotation> annotationClass;
    private final Class<?> rootInterface;

    public AbstractClientInterceptor(Class<? extends Annotation> annotationClass, Class<?> rootInterface) {
        this.annotationClass = annotationClass;
        this.rootInterface = rootInterface;
    }

    @Override
    public Pointcut getPointcut() {
        return new Pointcut() {
            @Override
            public ClassFilter getClassFilter() {
                return new RootClassFilter(rootInterface);
            }

            @Override
            public MethodMatcher getMethodMatcher() {
                return new MethodMatcher() {
                    @Override
                    public boolean matches(Method method, Class<?> targetClass) {
                        return AbstractClientInterceptor.this.checkMethod(method, targetClass);
                    }

                    @Override
                    public boolean isRuntime() {
                        return true;
                    }

                    @Override
                    public boolean matches(Method method, Class<?> targetClass, Object... args) {
                        return AbstractClientInterceptor.this.checkMethod(method, targetClass);
                    }
                };
            }
        };
    }

    private boolean checkMethod(Method method, Class<?> targetClass) {
        if (!this.rootInterface.isAssignableFrom(method.getDeclaringClass())
                || (method.getModifiers() & Modifier.PUBLIC) == 0) {
            return false;
        }

        if (method.isAnnotationPresent(annotationClass) || targetClass.isAnnotationPresent(annotationClass)) {
            return true;
        }

        for (Class<?> anInterface : targetClass.getInterfaces()) {
            if (rootInterface.isAssignableFrom(anInterface)) {
                if (anInterface.isAnnotationPresent(annotationClass)) {
                    return true;
                }

                try {
                    Method ifaceMethod = anInterface.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    return ifaceMethod.isAnnotationPresent(annotationClass);
                } catch (NoSuchMethodException ignore) {
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPerInstance() {
        return true;
    }
}
