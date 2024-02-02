/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.airiot.sdk.logger.suggestion;


import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 注册日志建议信息
 */
public class Suggestions {

    /**
     * 注册日志建议信息。
     * <br>
     * key 为异常类型, value 为建议信息.
     */
    private final static Map<Class<? extends Throwable>, Function<Throwable, String>> SUGGESTIONS = new HashMap<>();

    static {
        String enabled = System.getProperty("LOGGER_SUGGESTION_BUILTIN_ENABLED", "TRUE");
        if ("TRUE".equalsIgnoreCase(enabled)) {
            builtIn();
        }
    }

    static void builtIn() {
        register(UnknownHostException.class, exception -> "请检查主机地址是否正确以及DNS解析是否正常");
        register(BindException.class, exception -> "请检查端口是否被占用");
        register(ConnectException.class, exception -> "请检查访问地址、网络及防火墙是否配置正确");
        register(MalformedURLException.class, exception -> "请检查访问地址格式是否正确");
        register(SocketTimeoutException.class, exception -> "请检查访问地址、网络及防火墙是否配置正确");
        register(NoRouteToHostException.class, exception -> "请检查访问地址、网络及防火墙是否配置正确");
    }

    /**
     * 注册异常建议信息
     *
     * @param clazz      异常的类型
     * @param suggestion 建议信息
     */
    public static void register(Class<? extends Throwable> clazz, Function<Throwable, String> suggestion) {
        SUGGESTIONS.put(clazz, suggestion);
    }

    /**
     * 查询异常的建议信息
     *
     * @param cause 异常对象
     * @return 如果没有找到该异常对应的建议信息, 则返回 {@link Optional#empty()}
     */
    public static Optional<String> getSuggestion(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (SUGGESTIONS.containsKey(current.getClass())) {
                Function<Throwable, String> suggestion = SUGGESTIONS.get(current.getClass());
                return Optional.of(suggestion.apply(current));
            }

            for (Class<? extends Throwable> keyClass : SUGGESTIONS.keySet()) {
                if (keyClass.isInstance(current)) {
                    Function<Throwable, String> suggestion = SUGGESTIONS.get(keyClass);
                    return Optional.of(suggestion.apply(current));
                }
            }

            Throwable next = current.getCause();
            if (next == current) {
                break;
            }
            current = next;
        }
        return Optional.empty();
    }

    /**
     * 包装异常, 如果异常有建议信息, 则包装为 {@link SuggestionException}, 如果没有相应的建议信息则直接返回原始异常
     *
     * @param cause 原始异常
     * @return 包装后的异常. 如果没有相应的建议信息则直接返回原始异常
     */
    public static Throwable wrap(Throwable cause) {
        Optional<String> suggestion = getSuggestion(cause);
        if (suggestion.isPresent()) {
            return new SuggestionException(cause, suggestion.get());
        }
        return cause;
    }

    /**
     * 包装异常, 如果异常有建议信息, 则包装为 {@link SuggestionException}, 如果没有相应的建议信息则直接返回原始异常
     *
     * @param cause         原始异常
     * @param defSuggestion 默认建议信息
     * @return 包装后的异常. 如果没有相应的建议信息则使用默认建议信息
     */
    public static Throwable wrap(Throwable cause, String defSuggestion) {
        Optional<String> suggestion = getSuggestion(cause);
        return new SuggestionException(cause, suggestion.orElse(defSuggestion));
    }
}
