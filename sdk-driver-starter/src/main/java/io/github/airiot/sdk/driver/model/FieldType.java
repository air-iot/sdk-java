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

package io.github.airiot.sdk.driver.model;

import org.springframework.util.StringUtils;

import java.util.Optional;

public enum FieldType {
    STRING("string"),
    INTEGER("integer"),
    FLOAT("float"),
    BOOLEAN("boolean");

    // 成员变量
    private final String value;

    FieldType(String value) {
        this.value = value;
    }

    public boolean equals(String type) {
        return this.value.equalsIgnoreCase(type);
    }

    public String getValue() {
        return value;
    }
    
    public static Optional<FieldType> fromString(String type) {
        if (!StringUtils.hasText(type)) {
            return Optional.empty();
        }

        switch (type.toLowerCase()) {
            case "string":
                return Optional.of(STRING);
            case "integer":
                return Optional.of(INTEGER);
            case "float":
                return Optional.of(FLOAT);
            case "boolean":
                return Optional.of(BOOLEAN);
            default:
                return Optional.empty();
        }
    }
}

