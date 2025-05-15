package io.github.airiot.config;


import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(ModeCondition.class)
public @interface ModelConditionOnProperty {

    /**
     * 默认模式, 从 etcd 加载配置
     */
    String ETCD = "etcd";
    /**
     * lite 模式, 从环境变量加载配置
     */
    String LITE = "lite";

    /**
     * 期望的模式.
     * @see #ETCD
     * @see #LITE
     */
    String value();

    boolean matchIfMissing() default false;

}
