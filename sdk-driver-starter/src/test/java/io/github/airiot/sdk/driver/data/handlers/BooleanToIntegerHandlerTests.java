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

public class BooleanToIntegerHandlerTests {

    private final BooleanToIntegerHandler handler = new BooleanToIntegerHandler();

    @Test
    void testSupports() {
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new Tag(), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new Tag(), 123.456));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new Tag(), "true"));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new Tag(), true));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new Tag(), false));
    }

    @Test
    void testHandle() {
        Tag tag = new Tag("tag01", "tag01", null, null, 0, 0d);
        Assertions.assertEquals(Collections.singletonMap("tag01", 0), handler.handle("device-001", "dataPoint-1", tag, false));
        Assertions.assertEquals(Collections.singletonMap("tag01", 1), handler.handle("device-001", "dataPoint-1", tag, true));
    }
}
