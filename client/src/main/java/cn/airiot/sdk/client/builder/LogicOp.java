package cn.airiot.sdk.client.builder;


import java.util.Collections;

/**
 * 逻辑操作
 */
public enum LogicOp {

    /**
     * 比较
     */
    EQ(v -> v),

    /**
     * 不相等
     */
    NE(v -> Collections.singletonMap("$not", v)),

    /**
     * 小于
     */
    LT(v -> Collections.singletonMap("$lt", v)),
    /**
     * 小于或等于
     */
    LTE(v -> Collections.singletonMap("$lte", v)),

    /**
     * 大于
     */
    GT(v -> Collections.singletonMap("$gt", v)),
    /**
     * 大于或等于
     */
    GTE(v -> Collections.singletonMap("$gte", v)),

    /**
     * 在指定列表范围内
     */
    IN(v -> Collections.singletonMap("$in", v)),
    /**
     * 不在指定列表范围内
     */
    NIN(v -> Collections.singletonMap("$nin", v)),
    ;

    private final Segment segment;

    LogicOp(Segment segment) {
        this.segment = segment;
    }

    public <T> Object apply(T value) {
        return this.segment.apply(value);
    }
}
