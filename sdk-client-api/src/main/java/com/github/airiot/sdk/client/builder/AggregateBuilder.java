package com.github.airiot.sdk.client.builder;

import org.springframework.util.StringUtils;

import java.util.Collections;

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

    private Query.Builder op(String op, String alias) {
        return this.builder.summary(Collections.singletonMap(alias, Collections.singletonMap(op, this.field)));
    }

    private Query.Builder op(String op) {
        return op(op, this.field);
    }

    /**
     * 统计行数
     */
    public Query.Builder count() {
        return this.op("$count");
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
        return this.op("$count", alias);
    }

    /**
     * 最小值
     */
    public Query.Builder min() {
        return this.op("$min");
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
        return this.op("$min", alias);
    }

    /**
     * 最大值
     */
    public Query.Builder max() {
        return this.op("$max");
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
        return this.op("$max", alias);
    }

    /**
     * 平均值
     */
    public Query.Builder avg() {
        return this.op("$avg");
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
        return this.op("$avg", alias);
    }

    /**
     * 求和
     */
    public Query.Builder sum() {
        return this.op("$sum");
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
        return this.op("$sum", alias);
    }

    /**
     * 第一个值
     */
    public Query.Builder first() {
        return this.op("$first");
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
        return this.op("$first", alias);
    }

    /**
     * 最后一个值
     */
    public Query.Builder last() {
        return this.op("$last");
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
        return this.op("$last", alias);
    }
}
