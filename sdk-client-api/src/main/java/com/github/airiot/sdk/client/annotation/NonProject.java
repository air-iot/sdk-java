package com.github.airiot.sdk.client.annotation;


import com.github.airiot.sdk.client.context.RequestContext;
import com.github.airiot.sdk.client.service.PlatformClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 是否传递当前操作数据所属的项目信息. <br>
 * 平台客户端会自动拆拦截所有 {@link PlatformClient} 的实现类, 如果实现类或方法中带有该注解则不验证当前请求上下文中是否包含包含了 {@code projectId} 信息. <br>
 * 如果实现类或方法上没有该注解, 则会传递 {@link RequestContext#getProjectId()} 信息, 如果该信息为空, 则会抛出异常
 *
 * @see RequestContext
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonProject {
}
