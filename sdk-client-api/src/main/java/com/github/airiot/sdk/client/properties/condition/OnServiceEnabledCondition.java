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

package com.github.airiot.sdk.client.properties.condition;

import com.github.airiot.sdk.client.properties.ClientProperties;
import com.github.airiot.sdk.client.properties.ServiceType;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;


/**
 * 判断目标服务是否已经启用
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class OnServiceEnabledCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionalOnServiceEnabled.class.getName());
        ServiceType value = (ServiceType) annotationAttributes.get("value");
        String serviceName = value.getName().toLowerCase();
        boolean enabled = context.getEnvironment()
                .getProperty(String.format("%s.services.%s.enabled", ClientProperties.PREFIX, serviceName), Boolean.class, false);
        if (enabled) {
            return ConditionOutcome.match(String.format("the service %s is enabled", serviceName));
        }
        return ConditionOutcome.noMatch(String.format("the service %s is disabled", serviceName));
    }
}
