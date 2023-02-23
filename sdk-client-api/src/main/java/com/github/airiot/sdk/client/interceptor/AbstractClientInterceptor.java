package com.github.airiot.sdk.client.interceptor;

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
