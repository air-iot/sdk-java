package com.github.airiot.sdk.client.annotation;


import com.github.airiot.sdk.client.service.PlatformClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 是否禁用身份认证.<br>
 * 平台客户端会自动拆拦截所有 {@link PlatformClient} 的实现类, 如果实现类或方法中带有该注解则不执行认证相关操作
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DisableAuth {

    /**
     * 是否禁用身份认证
     *
     * @return {@code true} 表示禁用认证, {@code false} 表示启用认证
     */
    boolean value() default true;
}
