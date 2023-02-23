package com.github.airiot.sdk.client.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 工作表字段信息
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

    /**
     * 字段标识
     * <br>
     * 用于当属性名称与工作表定义中该字段的标识不一致时的场景
     *
     * @return 工作表中该字段的标识
     */
    String value();
}
