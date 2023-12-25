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

package io.github.airiot.sdk.encrypt;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.*;
import java.util.stream.Collectors;

public class Decryptors {

    private final static List<Decrypter> DECRYPTERS = new ArrayList<>();

    static {
        ServiceLoader<Decrypter> loader = ServiceLoader.load(Decrypter.class);
        for (Decrypter decrypter : loader) {
            DECRYPTERS.add(decrypter);
        }
        DECRYPTERS.sort(Comparator.comparingInt(Decrypter::getOrder));
    }

    /**
     * 判断是否有解密器
     *
     * @return 如果没有注册任何解密器, 则返回 true
     */
    public static boolean isEmpty() {
        return DECRYPTERS.isEmpty();
    }

    /**
     * 找到第一个支持的解密器
     *
     * @param key   属性的 key
     * @param value 属性的 value
     * @return 第一个支持的解密器. 如果没有对应的解密器, 则返回 {@link Optional#empty()}
     */
    public static Optional<Decrypter> getMatchedFirst(String key, String value) {
        for (Decrypter decrypter : DECRYPTERS) {
            if (decrypter.supports(key, value)) {
                return Optional.of(decrypter);
            }
        }
        return Optional.empty();
    }

    /**
     * 找到所有支持的解密器
     *
     * @param key   属性的 key
     * @param value 属性的 value
     * @return 所有支持的解密器. 如果没有对应的解密器, 则返回空列表
     */
    public static List<Decrypter> getMatchedAll(String key, String value) {
        return DECRYPTERS.stream()
                .filter(decrypter -> decrypter.supports(key, value))
                .collect(Collectors.toList());
    }

    /**
     * 对属性值进行解密
     *
     * @param key   属性的 key
     * @param value 属性的 value
     * @return 解密后的属性值. 如果没有对应的解密器, 则直接返回原始值
     */
    public static String decrypt(String key, String value) throws Exception {
        for (Decrypter decrypter : DECRYPTERS) {
            if (decrypter.supports(key, value)) {
                return decrypter.decrypt(key, value);
            }
        }
        return value;
    }

    /**
     * 对所有属性源中的属性值进行解密
     *
     * @param propertySources 属性源
     * @return 所有解密后的属性值集合, 只包含需要解密的属性值
     */
    public static Map<String, Object> decrypt(PropertySources propertySources) throws Exception {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<PropertySource<?>> sources = new ArrayList<>();
        for (PropertySource<?> source : propertySources) {
            sources.add(0, source);
        }
        for (PropertySource<?> source : sources) {
            merge(source, properties);
        }
        return properties;
    }

    static void merge(PropertySource<?> source, Map<String, Object> properties) throws Exception {
        if (source instanceof CompositePropertySource) {
            List<PropertySource<?>> sources = new ArrayList<>(((CompositePropertySource) source).getPropertySources());
            Collections.reverse(sources);
            for (PropertySource<?> nested : sources) {
                merge(nested, properties);
            }
        } else if (source instanceof EnumerablePropertySource) {
            EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
            for (String key : enumerable.getPropertyNames()) {
                Object property = source.getProperty(key);
                if (!(property instanceof String)) {
                    continue;
                }

                String value = property.toString();
                Optional<Decrypter> decryptor = Decryptors.getMatchedFirst(key, value);
                if (!decryptor.isPresent()) {
                    // 如果同一属性在多个 PropertySource 中都有定义, 优先级高的的属性值不需要解密时, 删除优先级中已解密的属性值.
                    // 例如:
                    // propertySource1:
                    //   key1: dell123
                    // propertySource0:
                    //   key1: ENC(ZGVsbDEyMw==)
                    properties.remove(key);
                    continue;
                }

                properties.put(key, decryptor.get().decrypt(key, value));
            }
        }
    }
}
