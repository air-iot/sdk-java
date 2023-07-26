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

import io.github.airiot.sdk.driver.model.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class ByteArrayToHexHandlerTests {

    private final ByteArrayToHexHandler handler = new ByteArrayToHexHandler();

    @Test
    void testSupports() {
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new Tag(), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new Tag(), 123.456));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new Tag(), "true"));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new Tag(), new Byte[]{}));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new Tag(), new byte[]{0x01, 0x02, 0x03, 0x04}));
    }
    
    @Test
    void testHandle() {
        Tag tag = new Tag("tag01", "tag01", null, null, 0, 0d);
        Assertions.assertEquals(Collections.singletonMap("tag01", "hex__"), handler.handle("device-001", "dataPoint-1", tag, new byte[]{}));
        Assertions.assertEquals(Collections.singletonMap("tag01", "hex__7e7e01605f87070004d22f000802002223063013472203ae10"), handler.handle("device-001", "dataPoint-1", tag, new byte[]{126, 126, 1, 96, 95, -121, 7, 0, 4, -46, 47, 0, 8, 2, 0, 34, 35, 6, 48, 19, 71, 34, 3, -82, 16}));
    }
}
