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
public class RandAndScaleValueHandlerTests {

    private final RoundAndScaleValueHandler handler = new RoundAndScaleValueHandler();

    @Test
    void testSupports() {
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", null, 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag(), 123));
        Assertions.assertFalse(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, null, null), 123));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, null, 0d), 123));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, 0, null), 123));
        Assertions.assertTrue(handler.supports("device-001", "dataPoint-1", new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, 0, 0d), 123));
    }

    @Test
    void testScaleValue() {
        io.github.airiot.sdk.driver.model.Tag tag = new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, null, 10d);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(500d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(1000d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(0.00001d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(999.99999d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        tag.setMod(0d);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        tag.setMod(0.1);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(5d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(10d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(0.0000001d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(9.9999999d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        tag.setMod(-0.1);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(-5d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(-10d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(-0.0000001d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(-9.9999999d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        tag.setMod(0.001);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(0.05d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(0.1d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(0.000000001d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(0.099999999d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        tag.setMod(-0.001);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(-0.05d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(-0.1d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(-0.000000001d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(-0.099999999d, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));


        tag.setMod(2d);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(100d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(200d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(0.000002d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(199.999998, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));

        tag.setMod(-2d);
        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(-100d, handler.handle("device-001", "dataPoint-1", tag, 50d));
        Assertions.assertEquals(-200d, handler.handle("device-001", "dataPoint-1", tag, 100d));
        Assertions.assertEquals(-0.000002d, handler.handle("device-001", "dataPoint-1", tag, 0.000001d));
        Assertions.assertEquals(-199.999998, handler.handle("device-001", "dataPoint-1", tag, 99.999999d));
    }

    @Test
    void testFixedValue() {
        io.github.airiot.sdk.driver.model.Tag tag = new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, 1, null);

        Assertions.assertEquals(0d, handler.handle("device-001", "dataPoint-1", tag, 0d));
        Assertions.assertEquals(10d, handler.handle("device-001", "dataPoint-1", tag, 10d));
        Assertions.assertEquals(10.0d, handler.handle("device-001", "dataPoint-1", tag, 10.0d));
        Assertions.assertEquals(10.1d, handler.handle("device-001", "dataPoint-1", tag, 10.1d));
        Assertions.assertEquals(10.4d, handler.handle("device-001", "dataPoint-1", tag, 10.4d));
        Assertions.assertEquals(10.5d, handler.handle("device-001", "dataPoint-1", tag, 10.5d));
        Assertions.assertEquals(10.9d, handler.handle("device-001", "dataPoint-1", tag, 10.9d));
        Assertions.assertEquals(10.9d, handler.handle("device-001", "dataPoint-1", tag, 10.94d));
        Assertions.assertEquals(11.0d, handler.handle("device-001", "dataPoint-1", tag, 10.95d));
        Assertions.assertEquals(10.9d, handler.handle("device-001", "dataPoint-1", tag, 10.85d));
        Assertions.assertEquals(10.9d, handler.handle("device-001", "dataPoint-1", tag, 10.945d));

        tag.setFixed(3);
        Assertions.assertEquals(10.945d, handler.handle("device-001", "dataPoint-1", tag, 10.945d));
        Assertions.assertEquals(10.945d, handler.handle("device-001", "dataPoint-1", tag, 10.9451d));
        Assertions.assertEquals(10.945d, handler.handle("device-001", "dataPoint-1", tag, 10.9454d));
        Assertions.assertEquals(10.946d, handler.handle("device-001", "dataPoint-1", tag, 10.9456d));

        tag.setFixed(0);
        Assertions.assertEquals(10d, handler.handle("device-001", "dataPoint-1", tag, 10.1d));
        Assertions.assertEquals(10d, handler.handle("device-001", "dataPoint-1", tag, 10.4d));
        Assertions.assertEquals(11d, handler.handle("device-001", "dataPoint-1", tag, 10.5d));
        Assertions.assertEquals(10d, handler.handle("device-001", "dataPoint-1", tag, 10.45d));
    }

    @Test
    void testScaleAndRound() {
        io.github.airiot.sdk.driver.model.Tag tag = new io.github.airiot.sdk.driver.model.Tag("tag01", "tag01", null, null, 2, 0.1);
        Assertions.assertEquals(1.01d, handler.handle("device-001", "dataPoint-1", tag, 10.1d));
        Assertions.assertEquals(1.01d, handler.handle("device-001", "dataPoint-1", tag, 10.145d));
        Assertions.assertEquals(1.02d, handler.handle("device-001", "dataPoint-1", tag, 10.15d));
        Assertions.assertEquals(1.1d, handler.handle("device-001", "dataPoint-1", tag, 10.95d));

        tag.setMod(10d);

        Assertions.assertEquals(101d, handler.handle("device-001", "dataPoint-1", tag, 10.1d));
        Assertions.assertEquals(101.45d, handler.handle("device-001", "dataPoint-1", tag, 10.145d));
        Assertions.assertEquals(101.5d, handler.handle("device-001", "dataPoint-1", tag, 10.15d));
        Assertions.assertEquals(109.54d, handler.handle("device-001", "dataPoint-1", tag, 10.954d));
        Assertions.assertEquals(109.55d, handler.handle("device-001", "dataPoint-1", tag, 10.9545d));
    }

    @Test
    void test01() {
        io.github.airiot.sdk.driver.model.Tag tag = new Tag("tag01", "tag01", null, null, 2, 0.1);
        Assertions.assertEquals(1801021.6d, handler.handle("device-001", "dataPoint-1", tag, 18010216));
    }

    @Test
    void testNaN() {
        io.github.airiot.sdk.driver.model.Tag tag = new Tag("tag01", "tag01", null, null, 2, 0.1);
        Assertions.assertNull(handler.handle("device-001", "dataPoint-1", tag, Double.NaN));
        Assertions.assertNull(handler.handle("device-001", "dataPoint-1", tag, Double.NEGATIVE_INFINITY));
        Assertions.assertNull(handler.handle("device-001", "dataPoint-1", tag, Double.POSITIVE_INFINITY));
    }
}
