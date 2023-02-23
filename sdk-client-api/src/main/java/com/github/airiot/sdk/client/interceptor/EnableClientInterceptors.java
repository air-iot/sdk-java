package com.github.airiot.sdk.client.interceptor;


import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用相关拦截器
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ClientInterceptorRegistrar.class)
public @interface EnableClientInterceptors {

}
