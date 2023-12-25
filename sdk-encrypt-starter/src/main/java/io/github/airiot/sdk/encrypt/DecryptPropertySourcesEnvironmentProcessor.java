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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

/**
 * PropertySource 属性值解密处理.
 */
public class DecryptPropertySourcesEnvironmentProcessor implements EnvironmentPostProcessor {

    private final static String PROPERTY_SOURCE_NAME = "airiot-encrypt";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (Decryptors.isEmpty()) {
            return;
        }

        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> properties = null;
        try {
            properties = Decryptors.decrypt(propertySources);
        } catch (Exception e) {
            throw new RuntimeException("decrypt the properties failed", e);
        }

        propertySources.remove(PROPERTY_SOURCE_NAME);
        if (!properties.isEmpty()) {
            propertySources.addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }
}
