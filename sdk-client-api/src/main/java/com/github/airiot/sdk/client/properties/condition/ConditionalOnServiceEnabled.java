package com.github.airiot.sdk.client.properties.condition;


import com.github.airiot.sdk.client.properties.ServiceType;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;


/**
 * 判断指定服务是否启用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnServiceEnabledCondition.class)
public @interface ConditionalOnServiceEnabled {
    
    /**
     * 服务类型
     */
    ServiceType value();
}
