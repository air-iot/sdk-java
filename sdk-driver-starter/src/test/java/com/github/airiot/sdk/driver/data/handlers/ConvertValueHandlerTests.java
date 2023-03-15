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

package com.github.airiot.sdk.driver.data.handlers;

import com.github.airiot.sdk.driver.model.Tag;
import com.github.airiot.sdk.driver.model.TagValue;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("unittest")
public class ConvertValueHandlerTests {

    private final ConvertValueHandler handler = new ConvertValueHandler();

    @Test
    void testSupports() {
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", null, 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new com.github.airiot.sdk.driver.model.Tag(), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new com.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new com.github.airiot.sdk.driver.model.Tag("tag01", "tag01", new TagValue(), null, 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new com.github.airiot.sdk.driver.model.Tag("tag01", "tag01", new TagValue(null, 2d, 3d, 4d), null, 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new com.github.airiot.sdk.driver.model.Tag("tag01", "tag01", new TagValue(1d, null, 3d, 4d), null, 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new com.github.airiot.sdk.driver.model.Tag("tag01", "tag01", new TagValue(1d, 2d, null, 4d), null, 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new com.github.airiot.sdk.driver.model.Tag("tag01", "tag01", new TagValue(1d, 2d, 3d, null), null, 0, 0d), 123));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new com.github.airiot.sdk.driver.model.Tag("tag01", "tag01", new TagValue(1d, 2d, 3d, 4d), null, 0, 0d), 123));
    }

    @Test
    void testHandle() {
        com.github.airiot.sdk.driver.model.Tag tag = new Tag("tag01", "tag01", new TagValue(-50d, 50d, -5000d, 5000d), null, 0, 0d);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(50d, handler.handle("device-001", "dataPoint-1", tag, 5000d));
        Assertions.assertEquals(-50d, handler.handle("device-001", "dataPoint-1", tag, -5000d));
        Assertions.assertEquals(12.34d, handler.handle("device-001", "dataPoint-1", tag, 1234d));
        Assertions.assertEquals(-12.34d, handler.handle("device-001", "dataPoint-1", tag, -1234d));

        // 超出边界值
        Assertions.assertEquals(50d, handler.handle("device-001", "dataPoint-1", tag, 5001d));
        Assertions.assertEquals(-50d, handler.handle("device-001", "dataPoint-1", tag, -5001d));
    }
}
