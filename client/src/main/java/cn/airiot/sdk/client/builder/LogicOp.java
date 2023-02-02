package cn.airiot.sdk.client.builder;


import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * 逻辑操作
 */
public enum LogicOp {

    /**
     * 比较
     */
    EQ(LogicOp::checkAndGetFist),

    /**
     * 不相等
     */
    NE(v -> Collections.singletonMap("$not", checkAndGetFist(v))),

    /**
     * 小于
     */
    LT(v -> Collections.singletonMap("$lt", checkAndGetFist(v))),
    /**
     * 小于或等于
     */
    LTE(v -> Collections.singletonMap("$lte", checkAndGetFist(v))),

    /**
     * 大于
     */
    GT(v -> Collections.singletonMap("$gt", checkAndGetFist(v))),
    /**
     * 大于或等于
     */
    GTE(v -> Collections.singletonMap("$gte", checkAndGetFist(v))),

    BETWEEN(v -> {
        checkSize(2, v);
        Map<String, Object> data = Maps.newHashMapWithExpectedSize(2);
        data.put("$gte", v[0]);
        data.put("$lt", v[1]);
        return data;
    }),

    /**
     * 在指定列表范围内
     */
    IN(v -> Collections.singletonMap("$in", checkAndGetFist(v))),
    /**
     * 不在指定列表范围内
     */
    NOT_IN(v -> Collections.singletonMap("$nin", checkAndGetFist(v))),
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

    static Object checkAndGetFist(Object... values) {
        checkSize(1, values);
        return values[0];
    }

    private final LogicSegment logicSegment;

    LogicOp(LogicSegment logicSegment) {
        this.logicSegment = logicSegment;
    }

    public <T> Object apply(T... values) {
        return this.logicSegment.apply(values);
    }
}
