package com.github.airiot.sdk.client.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 工作表信息
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WorkTable {
    /**
     * 实体对应的表名
     *
     * @return 表名
     */
    String value();
}
