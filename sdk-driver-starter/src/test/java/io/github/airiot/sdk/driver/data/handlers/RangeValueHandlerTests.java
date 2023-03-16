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

import io.github.airiot.sdk.driver.model.Range;
import io.github.airiot.sdk.driver.model.Tag;
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
public class RangeValueHandlerTests {

    private final RangeValueHandler handler = new RangeValueHandler();

    @Test
    void testSupports() {
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", null, 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag(), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(), 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(null, 2d, 3d, ""), 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(1d, null, 3d, ""), 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(1d, 2d, null, ""), 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(1d, 2d, 3d, ""), 0, 0d), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(1d, 2d, 3d, "missing"), 0, 0d), 123));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(1d, 2d, 3d, "fixed"), 0, 0d), 123));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(1d, 2d, 3d, "boundary"), 0, 0d), 123));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(1d, 2d, 3d, "discard"), 0, 0d), 123));
    }

    @Test
    void testFixedValue() {
        io.github.airiot.sdk.driver.model.Tag tag = new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(0d, 100d, 200d, "fixed"), 0, 0d);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(50d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(100d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(0.000001d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(99.999999d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        // 超出边界值
        Assertions.assertEquals(200d, handler.handle("device-001", "dataPoint-1", tag, -1d));
        Assertions.assertEquals(200d, handler.handle("device-001", "dataPoint-1", tag, 101d));
    }

    @Test
    void testBoundaryValue() {
        io.github.airiot.sdk.driver.model.Tag tag = new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, new Range(0d, 100d, 200d, "boundary"), 0, 0d);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(50d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(100d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(0.000001d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(99.999999d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        // 超出边界值
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, -1d));
        Assertions.assertEquals(100d, handler.handle("device-001", "dataPoint-1", tag, 101d));
    }

    @Test
    void testDiscardValue() {
        io.github.airiot.sdk.driver.model.Tag tag = new Tag("tag01", "tag01", null, new Range(0d, 100d, 200d, "discard"), 0, 0d);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(50d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(100d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(0.000001d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(99.999999d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        // 超出边界值
        Assertions.assertNull(handler.handle("device-001", "dataPoint-1", tag, -1d));
        Assertions.assertNull(handler.handle("device-001", "dataPoint-1", tag, 101d));
    }
}
