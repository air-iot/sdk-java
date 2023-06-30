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

package io.github.airiot.sdk.client.builder;


import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * 逻辑操作
 */
public enum LogicOp {

    /**
     * 比较
     */
    EQ(LogicOp::checkAndGetFirst),

    /**
     * 不相等
     */
    NE(v -> Collections.singletonMap("$not", checkAndGetFirst(v))),

    /**
     * 小于
     */
    LT(v -> Collections.singletonMap("$lt", checkAndGetFirst(v))),
    /**
     * 小于或等于
     */
    LTE(v -> Collections.singletonMap("$lte", checkAndGetFirst(v))),

    /**
     * 大于
     */
    GT(v -> Collections.singletonMap("$gt", checkAndGetFirst(v))),
    /**
     * 大于或等于
     */
    GTE(v -> Collections.singletonMap("$gte", checkAndGetFirst(v))),
    /**
     * 正则匹配
     */
    REGEX(v -> Collections.singletonMap("$regex", checkAndGetFirst(v))),

    /**
     * 在指定范围内, 闭区间. [minValue, maxValue]
     */
    BETWEEN(v -> {
        checkSize(2, v);
        Map<String, Object> data = Maps.newHashMapWithExpectedSize(2);
        data.put("$gte", v[0]);
        data.put("$lte", v[1]);
        return data;
    }),

    /**
     * 在指定范围内, 左开右闭区间. (minValue, maxValue]
     */
    BETWEEN_EXCLUDE_LEFT(v -> {
        checkSize(2, v);
        Map<String, Object> data = Maps.newHashMapWithExpectedSize(2);
        data.put("$gt", v[0]);
        data.put("$lte", v[1]);
        return data;
    }),

    /**
     * 在指定范围内, 左闭右开区间. [minValue, maxValue)
     */
    BETWEEN_EXCLUDE_RIGHT(v -> {
        checkSize(2, v);
        Map<String, Object> data = Maps.newHashMapWithExpectedSize(2);
        data.put("$gte", v[0]);
        data.put("$lt", v[1]);
        return data;
    }),

    /**
     * 在指定范围内, 左开右开区间. (minValue, maxValue)
     */
    BETWEEN_EXCLUDE_ALL(v -> {
        checkSize(2, v);
        Map<String, Object> data = Maps.newHashMapWithExpectedSize(2);
        data.put("$gt", v[0]);
        data.put("$lt", v[1]);
        return data;
    }),

    /**
     * 在指定列表范围内
     */
    IN(v -> Collections.singletonMap("$in", checkAndGetFirst(v))),
    /**
     * 不在指定列表范围内
     */
    NOT_IN(v -> Collections.singletonMap("$nin", checkAndGetFirst(v))),
    ;

    /**
     * 检查参数数量
     *
     * @param expectedSize 预望的参数数量
     * @param values       参数列表
     * @throws IllegalArgumentException 如果传入的参数数量与期望的参数数量不一致
     */
    static void checkSize(int expectedSize, Object... values) throws IllegalArgumentException {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("expectedSize cannot less than 0");
        }

        if (expectedSize == 0 && (values == null || values.length == 0)) {
            return;
        }

        if ((expectedSize != 0 && values == null) || expectedSize != values.length) {
            throw new IllegalArgumentException("the size of arguments is not matched with expected size " + expectedSize);
        }
    }

    static Object checkAndGetFirst(Object... values) {
        checkSize(1, values);
        return values[0];
    }

    private final LogicSegment logicSegment;

    LogicOp(LogicSegment logicSegment) {
        this.logicSegment = logicSegment;
    }

    public final <T> Object apply(@Nullable T... values) {
        return this.logicSegment.apply(values);
    }
}
