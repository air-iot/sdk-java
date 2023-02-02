package cn.airiot.sdk.client.builder;

import org.springframework.util.StringUtils;

public class AggregateBuilder {

    private final Query.Builder builder;
    private final String field;

    public AggregateBuilder(Query.Builder builder, String field) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("the aggregate 'field' cannot be null or empty");
        }
        this.builder = builder;
        this.field = field;
    }

    /**
     * 统计行数
     */
    public Query.Builder count() {
        return builder.select(AggregateOp.COUNT.apply(field, field));
    }

    /**
     * 统计列别名
     *
     * @param alias 别名
     */
    public Query.Builder count(String alias) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the 'alias' of count(" + field + ") cannot be null or empty");
        }
        return builder.select(AggregateOp.COUNT.apply(field, alias));
    }

    /**
     * 最小值
     */
    public Query.Builder min() {
        return builder.select(AggregateOp.MIN.apply(field, field));
    }

    /**
     * 最小值
     *
     * @param alias 别名
     */
    public Query.Builder min(String alias) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the 'alias' of min(" + field + ") cannot be null or empty");
        }
        return builder.select(AggregateOp.MIN.apply(field, alias));
    }

    /**
     * 最大值
     */
    public Query.Builder max() {
        return builder.select(AggregateOp.MAX.apply(field));
    }

    /**
     * 最大值
     *
     * @param alias 别名
     */
    public Query.Builder max(String alias) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the 'alias' of max(" + field + ") cannot be null or empty");
        }
        return builder.select(AggregateOp.MAX.apply(field, alias));
    }

    /**
     * 平均值
     */
    public Query.Builder avg() {
        return builder.select(AggregateOp.AVG.apply(field));
    }

    /**
     * 平均值
     *
     * @param alias 别名
     */
    public Query.Builder avg(String alias) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the 'alias' of avg(" + field + ") cannot be null or empty");
        }
        return builder.select(AggregateOp.AVG.apply(field, alias));
    }

    /**
     * 求和
     */
    public Query.Builder sum() {
        return builder.select(AggregateOp.SUM.apply(field));
    }

    /**
     * 求和
     *
     * @param alias 别名
     */
    public Query.Builder sum(String alias) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the 'alias' of sum(" + field + ") cannot be null or empty");
        }
        return builder.select(AggregateOp.SUM.apply(field, alias));
    }

    /**
     * 第一个值
     */
    public Query.Builder first() {
        return builder.select(AggregateOp.FIRST.apply(field));
    }

    /**
     * 第一个值
     *
     * @param alias 别名
     */
    public Query.Builder first(String alias) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the 'alias' of first(" + field + ") cannot be null or empty");
        }
        return builder.select(AggregateOp.FIRST.apply(field, alias));
    }

    /**
     * 最后一个值
     */
    public Query.Builder last() {
        return builder.select(AggregateOp.LAST.apply(field));
    }

    /**
     * 最后一个值
     *
     * @param alias 别名
     */
    public Query.Builder last(String alias) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the 'alias' of last(" + field + ") cannot be null or empty");
        }
        return builder.select(AggregateOp.LAST.apply(field, alias));
    }
}
