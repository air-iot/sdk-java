package io.github.airiot.sdk.datarelay.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP 请求代理注解. 用于标记方法代理的请求类型
 *
 * <pre>
 *
 *     public class MyDataRelayApp implements DataRelayApp {
 *
 *         public void start(String config) {
 *             // ...
 *         }
 *
 *         public void stop() {
 *
 *         }
 *
 *          // 代理 requestType 为 "proxy1" 的请求
 *          &#064;HttpProxy("proxy1")
 *         public Object proxy1(Map<String, List<String>> headers, String requestData) {
 *
 *         }
 *     }
 *
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpProxy {

    /**
     * 请求类型
     */
    String value();
}
