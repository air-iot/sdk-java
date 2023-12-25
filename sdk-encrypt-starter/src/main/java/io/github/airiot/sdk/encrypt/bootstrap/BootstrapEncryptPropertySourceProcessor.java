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

package io.github.airiot.sdk.encrypt.bootstrap;

import io.github.airiot.sdk.encrypt.Decryptors;
import org.springframework.cloud.bootstrap.encrypt.AbstractEnvironmentDecrypt;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

public class BootstrapEncryptPropertySourceProcessor extends AbstractEnvironmentDecrypt
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private final static String PROPERTY_SOURCE_NAME = "airiot-bootstrap-encrypt";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (Decryptors.isEmpty()) {
            return;
        }

        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
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

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
