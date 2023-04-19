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

package io.github.airiot.sdk.driver.data.handlers;

import io.github.airiot.sdk.driver.data.DataHandler;
import io.github.airiot.sdk.driver.model.Tag;


/**
 * 将 boolean 值转换为 0 或 1
 */
public class BooleanToIntegerHandler implements DataHandler {

    @Override
    public <T extends Tag> boolean supports(String tableId, String deviceId, T tag, Object value) {
        if (!DataHandler.super.supports(tableId, deviceId, tag, value)) {
            return false;
        }
        return value instanceof Boolean;
    }

    @Override
    public <T extends Tag> Object handle(String tableId, String deviceId, T tag, Object value) {
        return (Boolean) value ? 1 : 0;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 100;
    }
}
