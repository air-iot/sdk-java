package io.github.airiot.sdk.datarelay.application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.airiot.sdk.datarelay.annotation.HttpProxy;
import org.springframework.util.StringUtils;

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DataRelayAppProxy implements DataRelayApp<String> {

    private final DataRelayApp<Object> delegate;
    /**
     * 配置类型
     */
    private Type configClazz;
    private final Map<String, ProxyMethod> proxyMethods = new HashMap<>(3);
    private final Gson gson = new GsonBuilder().create();

    public DataRelayAppProxy(DataRelayApp<Object> delegate) {
        this.delegate = delegate;

        Class<?> delegateClazz = delegate.getClass();
        Type[] genTypes = delegateClazz.getGenericInterfaces();
        if (genTypes.length == 0) {
            throw new IllegalArgumentException("未找到 '" + delegate.getClass() + "' 的泛型类型");
        }

        for (Type genType : genTypes) {
            if (genType instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) genType;
                Type raw = pType.getRawType();
                if (raw instanceof Class && DataRelayApp.class.isAssignableFrom((Class<?>) raw)) {
                    this.configClazz = pType.getActualTypeArguments()[0];
                    break;
                }
            }
        }

        for (Method method : delegateClazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(HttpProxy.class)) {
                continue;
            }

            String methodName = method.getName();
            HttpProxy proxyAnn = method.getAnnotation(HttpProxy.class);
            if (!StringUtils.hasText(proxyAnn.value())) {
                throw new IllegalArgumentException("代理方法 '" + methodName + "' 上添加的 @HttpProxy 配置的请求类型为空");
            }

            String requestType = proxyAnn.value().trim().toUpperCase();
            if (proxyMethods.containsKey(requestType)) {
                throw new IllegalArgumentException("请求类型 '" + proxyAnn.value() + "' 已经被方法 '" + proxyMethods.get(requestType).getName() + "' 代理");
            }

            if (!method.getReturnType().isAssignableFrom(Object.class)) {
                throw new IllegalArgumentException("请求代理方法 '" + methodName + "' 的返回值类型不匹配, 返回值类型必须为 Object");
            }

            if (method.getParameterCount() != 2) {
                throw new IllegalArgumentException("请求代理方法 '" + methodName + "' 的参数列表不匹配, 必须为 public Object " + methodName + "(Map<String, List<String>> headers, String requestData)");
            }

            Parameter[] parameters = method.getParameters();
            if (!parameters[0].getType().isAssignableFrom(Map.class) || !parameters[1].getType().isAssignableFrom(String.class)) {
                throw new IllegalArgumentException("请求代理方法 '" + methodName + "' 的参数列表不匹配, 必须为 public Object " + methodName + "(Map<String, List<String>> headers, String requestData)");
            }

            this.proxyMethods.put(requestType, new ProxyMethod(this.delegate, method, requestType));
        }
    }

    @Override
    public void start(String config) throws Exception {
        if (this.configClazz instanceof Class) {
            Class<?> clazz = (Class<?>) this.configClazz;
            if (clazz.isAssignableFrom(String.class)) {
                this.delegate.start(config);
                return;
            } else if (clazz.isAssignableFrom(byte[].class)) {
                this.delegate.start(config.getBytes(StandardCharsets.UTF_8));
                return;
            }
        }

        Object configData = this.gson.fromJson(config, this.configClazz);
        this.delegate.start(configData);
    }

    @Override
    public void stop() {
        this.delegate.stop();
    }

    @Override
    public Object proxy(String requestType, Map<String, List<String>> headers, String requestData) throws Exception {
        if (this.proxyMethods.containsKey(requestType)) {
            return this.proxyMethods.get(requestType).invoke(headers, requestData);
        }
        return this.delegate.proxy(requestType, headers, requestData);
    }

    class ProxyMethod {
        final Object target;
        final Method method;
        final String requestType;
        final Class<?> requestDataType;

        String getName() {
            return this.method.getName();
        }

        public ProxyMethod(Object target, Method method, String requestType) {
            this.target = target;
            this.method = method;
            this.requestType = requestType;
            this.requestDataType = method.getParameters()[1].getType();
            this.method.setAccessible(true);
        }

        Object invoke(Map<String, List<String>> headers, String config) throws InvocationTargetException, IllegalAccessException {
            if (requestDataType.isAssignableFrom(String.class)) {
                return this.method.invoke(this.target, headers, config);
            } else if (requestDataType.isAssignableFrom(Map.class)) {
                Object configData = DataRelayAppProxy.this.gson.fromJson(config, TypeToken.getParameterized(Map.class, String.class, Object.class));
                return this.method.invoke(this.target, headers, configData);
            } else {
                Object configData = DataRelayAppProxy.this.gson.fromJson(config, this.requestDataType);
                return this.method.invoke(this.target, headers, configData);
            }
        }
    }
}
