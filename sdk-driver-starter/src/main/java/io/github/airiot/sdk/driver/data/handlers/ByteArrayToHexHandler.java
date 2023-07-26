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
import org.apache.commons.codec.binary.Hex;

import java.util.Collections;
import java.util.Map;


/**
 * 将字节数组转换为 hex 格式字符串, 并且添加 <b>hex__</b> 前缀.
 */
public class ByteArrayToHexHandler implements DataHandler {

    public static final String PREFIX = "hex__";

    @Override
    public <T extends Tag> boolean supports(String tableId, String deviceId, T tag, Object value) {
        if (!DataHandler.super.supports(tableId, deviceId, tag, value)) {
            return false;
        }
        return value instanceof byte[];
    }

    @Override
    public <T extends Tag> Map<String, Object> handle(String tableId, String deviceId, T tag, Object value) {
        return Collections.singletonMap(tag.getId(), PREFIX + Hex.encodeHexString((byte[]) value));
    }
    
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 99;
    }
}
