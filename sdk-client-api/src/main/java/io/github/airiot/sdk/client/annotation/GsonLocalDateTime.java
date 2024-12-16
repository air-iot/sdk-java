package io.github.airiot.sdk.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GsonLocalDateTime {

    String value() default "yyyy-MM-dd HH:mm:ss";
    
}
