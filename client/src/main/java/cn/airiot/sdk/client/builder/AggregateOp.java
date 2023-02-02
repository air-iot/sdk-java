package cn.airiot.sdk.client.builder;


import java.util.Collections;
import java.util.Map;

/**
 * 聚合操作
 */
public enum AggregateOp {
    /**
     * 统计数量
     */
    COUNT((f, a) -> Collections.singletonMap(a, Collections.singletonMap("$count", f))),
    /**
     * 平均数
     */
    AVG((f, a) -> Collections.singletonMap(a, Collections.singletonMap("$avg", f))),
    /**
     * 最大值
     */
    MAX((f, a) -> Collections.singletonMap(a, Collections.singletonMap("$max", f))),
    /**
     * 最小值
     */
    MIN((f, a) -> Collections.singletonMap(a, Collections.singletonMap("$min", f))),
    /**
     * 求和
     */
    SUM((f, a) -> Collections.singletonMap(a, Collections.singletonMap("$sum", f))),
    /**
     * 第一个值
     */
    FIRST((f, a) -> Collections.singletonMap(a, Collections.singletonMap("$first", f))),
    /**
     * 最后一个值
     */
    LAST((f, a) -> Collections.singletonMap(a, Collections.singletonMap("$last", f)));

    private final AggregateSegment segment;

    AggregateOp(AggregateSegment segment) {
        this.segment = segment;
    }

    public Map<String, Object> apply(String field, String alias) {
        return this.segment.apply(field, alias);
    }

    public Map<String, Object> apply(String field) {
        return this.segment.apply(field, field);
    }
}
