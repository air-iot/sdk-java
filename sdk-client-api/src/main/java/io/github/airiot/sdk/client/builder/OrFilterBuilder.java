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

import org.springframework.util.StringUtils;

import java.util.*;

public class OrFilterBuilder {

    private final Query.Builder builder;
    private final List<Map<String, Object>> filters;

    public OrFilterBuilder(Query.Builder builder, List<Map<String, Object>> filters) {
        this.builder = builder;
        this.filters = filters;
    }

    /**
     * 结束查询条件构建, 并返回查询构建器
     */
    public Query.Builder end() {
        return this.builder;
    }

    /**
     * 等于
     *
     * @param field 字段名
     * @param value 字段值
     */
    public <T> OrFilterBuilder eq(String field, T value) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'eq' cannot be empty");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.EQ.apply(value)));

        return this;
    }

    public <Type, T> OrFilterBuilder eq(SFunction<Type, ?> column, T value) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.eq(propName, value);
    }

    /**
     * 不等于
     *
     * @param field 字段名
     * @param value 字段值
     */
    public <T> OrFilterBuilder ne(String field, T value) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'ne' cannot be empty");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.NE.apply(value)));
        return this;
    }

    /**
     * 不等于
     *
     * @see #ne(String, Object)
     */
    public <Type, T> OrFilterBuilder ne(SFunction<Type, ?> column, T value) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.ne(propName, value);
    }

    /**
     * 在指定列表之内
     *
     * @see #in(String, Object[])
     */
    public <T> OrFilterBuilder in(String field, T... value) {
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException("Query: the field[" + field + "] values of condition 'in' cannot be null and  empty");
        }

        if (value.length == 1 && value[0] instanceof Collection) {
            return this.in(field, (Collection<T>) value[0]);
        }

        return this.in(field, Arrays.asList(value));
    }

    /**
     * 在指定列表之内
     *
     * @see #in(String, Object[])
     */
    public <Type, T> OrFilterBuilder in(SFunction<Type, ?> column, T value) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.in(propName, value);
    }

    /**
     * 在指定列表之内
     *
     * @param field 字段名
     * @param value 字段值列表
     */
    public <T> OrFilterBuilder in(String field, Collection<T> value) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'in' cannot be empty");
        }

        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Query: the field[" + field + "] values of condition 'in' cannot be null and  empty");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.IN.apply(value)));

        return this;
    }

    /**
     * 不在指定列表之内
     *
     * @see #notIn(String, Object...)
     */
    public <T> OrFilterBuilder notIn(String field, T... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Query: the field[" + field + "] values of condition 'not in' cannot be null and empty");
        }
        return this.notIn(field, Arrays.asList(values));
    }

    /**
     * 不在指定列表之内
     *
     * @see #notIn(String, Object...)
     */
    public <Type, T> OrFilterBuilder notIn(SFunction<Type, ?> column, T... values) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.notIn(propName, values);
    }

    /**
     * 不在指定列表之内
     *
     * @param field  字段名
     * @param values 字段值列表
     */
    public <T> OrFilterBuilder notIn(String field, Collection<T> values) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'not in' cannot be empty");
        }

        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Query: the field[" + field + "] values of condition 'not in' cannot be null and empty");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.NOT_IN.apply(values)));

        return this;
    }

    /**
     * 正则表达式匹配
     *
     * @see #regex(String, String)
     */
    public <Type> OrFilterBuilder regex(SFunction<Type, ?> column, String regex) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.regex(propName, regex);
    }

    /**
     * 正则表达式匹配
     *
     * @param field 字段名
     * @param regex 正则表达式
     */
    public OrFilterBuilder regex(String field, String regex) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'not in' cannot be empty");
        }

        if (!StringUtils.hasText(regex)) {
            throw new IllegalArgumentException("Query: the regex expression of condition 'regex' cannot be empty");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.REGEX.apply(regex)));

        return this;
    }

    /**
     * 小于
     *
     * @param field 字段名
     * @param value 字段值
     */
    public OrFilterBuilder lt(String field, Object value) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'lt' cannot be empty");
        }

        if (value == null) {
            throw new IllegalArgumentException("Query: the field[" + field + "] value of condition 'lt' cannot be null");
        }

        field = field.trim();
        final String fieldName = field;

        Object filter = this.filters.stream().filter(map -> map.containsKey(fieldName)).map(map -> map.get(fieldName));
        if (!(filter instanceof Map)) {
            this.filters.add(Collections.singletonMap(field, LogicOp.LT.apply(value)));
            return this;
        }

        Map<String, Object> map = new HashMap<>((Map<String, Object>) filter);
        map.putAll((Map<String, ?>) LogicOp.LT.apply(value));
        this.filters.add(Collections.singletonMap(field, map));
        return this;
    }

    /**
     * 小于
     *
     * @see #lt(String, Object)
     */
    public <T> OrFilterBuilder lt(SFunction<T, ?> column, Object value) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.lt(propName, value);
    }

    /**
     * 小于或等于
     *
     * @param field 字段名
     * @param value 字段值
     */
    public OrFilterBuilder lte(String field, Object value) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'lte' cannot be empty");
        }

        if (value == null) {
            throw new IllegalArgumentException("Query: the field[" + field + "] value of condition 'lte' cannot be null");
        }

        field = field.trim();
        final String fieldName = field;

        Object filter = this.filters.stream().filter(map -> map.containsKey(fieldName)).map(map -> map.get(fieldName));
        if (!(filter instanceof Map)) {
            this.filters.add(Collections.singletonMap(field, LogicOp.LTE.apply(value)));
            return this;
        }

        Map<String, Object> map = new HashMap<>((Map<String, Object>) filter);
        map.putAll((Map<String, ?>) LogicOp.LTE.apply(value));
        this.filters.add(Collections.singletonMap(field, map));
        return this;
    }

    /**
     * 小于或等于
     *
     * @see #lte(String, Object)
     */
    public <T> OrFilterBuilder lte(SFunction<T, ?> column, Object value) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.lte(propName, value);
    }

    /**
     * 大于
     *
     * @param field 字段名
     * @param value 字段值
     */
    public OrFilterBuilder gt(String field, Object value) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'gt' cannot be empty");
        }

        if (value == null) {
            throw new IllegalArgumentException("Query: the field[" + field + "] value of condition 'gt' cannot be null");
        }

        field = field.trim();
        final String fieldName = field;

        Object filter = this.filters.stream().filter(map -> map.containsKey(fieldName)).map(map -> map.get(fieldName));
        if (!(filter instanceof Map)) {
            this.filters.add(Collections.singletonMap(field, LogicOp.GT.apply(value)));
            return this;
        }

        Map<String, Object> map = new HashMap<>((Map<String, Object>) filter);
        map.putAll((Map<String, ?>) LogicOp.GT.apply(value));
        this.filters.add(Collections.singletonMap(field, map));
        return this;
    }

    /**
     * 大于
     *
     * @see #gt(String, Object)
     */
    public <T> OrFilterBuilder gt(SFunction<T, ?> column, Object value) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.gt(propName, value);
    }

    /**
     * 大于或等于
     *
     * @param field 字段名
     * @param value 字段值
     */
    public OrFilterBuilder gte(String field, Object value) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'gte' cannot be empty");
        }

        if (value == null) {
            throw new IllegalArgumentException("Query: the field[" + field + "] value of condition 'gte' cannot be null");
        }

        field = field.trim();
        final String fieldName = field;

        Object filter = this.filters.stream().filter(map -> map.containsKey(fieldName)).map(map -> map.get(fieldName));
        if (!(filter instanceof Map)) {
            this.filters.add(Collections.singletonMap(field, LogicOp.GTE.apply(value)));
            return this;
        }

        Map<String, Object> map = new HashMap<>((Map<String, Object>) filter);
        map.putAll((Map<String, ?>) LogicOp.GTE.apply(value));
        this.filters.add(Collections.singletonMap(field, map));
        return this;
    }

    /**
     * 大于或等于
     *
     * @see #gte(String, Object)
     */
    public <T> OrFilterBuilder gte(SFunction<T, ?> column, Object value) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.gte(propName, value);
    }

    /**
     * 在指定取值范围之内, 左闭右闭. 即: [minValue, maxValue].
     *
     * <pre>
     *     例如:
     *     // 查询年龄在 15 - 30 岁之间的用户. 包括 30 岁
     *     Query query = Query.newBuilder()
     *          .select(User.class)
     *          .between("age", 15, 30)
     *          .build();
     *
     *     // 查询 2020 年新注册的用户数
     *     Query query = Query.newBuilder()
     *          .groupField("count(id) as count")
     *          .between("createTime", "2020-01-01 00:00:00", "2020-12-31 23:59:59")
     *          .build();
     * </pre>
     *
     * @param field    字段名
     * @param minValue 最小值. 包含最小值
     * @param maxValue 最大值. 包含最大值
     */
    public OrFilterBuilder between(String field, Object minValue, Object maxValue) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'between' cannot be empty");
        }

        if (minValue == null || maxValue == null) {
            throw new IllegalArgumentException("Query: the field[" + field + "] range value [" + minValue + "," + maxValue + "] of condition 'between' cannot be null");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.BETWEEN.apply(minValue, maxValue)));

        return this;
    }

    /**
     * 在指定取值范围之内, 包含最小值和最大值. 即: [minValue, maxValue].
     *
     * @see #between(String, Object, Object)
     */
    public <T> OrFilterBuilder between(SFunction<T, ?> column, Object minValue, Object maxValue) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.between(propName, minValue, maxValue);
    }

    /**
     * 在指定取值范围之内, 左开右闭. 即: (minValue, maxValue].
     *
     * <pre>
     *     例如:
     *     // 查询年龄在 15 - 30 岁之间的用户. 不包括 15 岁
     *     Query query = Query.newBuilder()
     *          .select(User.class)
     *          .between("age", 15, 30)
     *          .build();
     *
     *     // 查询 2020 年新注册的用户数
     *     Query query = Query.newBuilder()
     *          .groupField("count(id) as count")
     *          .between("createTime", "2020-01-01 00:00:00", "2020-12-31 23:59:59")
     *          .build();
     * </pre>
     *
     * @param field    字段名
     * @param minValue 最小值. 不包含最小值
     * @param maxValue 最大值. 包含最大值
     */
    public OrFilterBuilder betweenExcludeLeft(String field, Object minValue, Object maxValue) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'betweenExcludeLeft' cannot be empty");
        }

        if (minValue == null || maxValue == null) {
            throw new IllegalArgumentException("Query: the field[" + field + "] range value [" + minValue + "," + maxValue + "] of condition 'between' cannot be null");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.BETWEEN_EXCLUDE_LEFT.apply(minValue, maxValue)));

        return this;
    }

    /**
     * 在指定取值范围之内, 左开右闭. 即: (minValue, maxValue].
     *
     * @see #betweenExcludeLeft(String, Object, Object)
     */
    public <T> OrFilterBuilder betweenExcludeLeft(SFunction<T, ?> column, Object minValue, Object maxValue) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.betweenExcludeLeft(propName, minValue, maxValue);
    }

    /**
     * 在指定取值范围之内, 左闭右开. 即: [minValue, maxValue).
     *
     * <pre>
     *     例如:
     *     // 查询年龄在 15 - 30 岁之间的用户. 不包括 30 岁
     *     Query query = Query.newBuilder()
     *          .select(User.class)
     *          .between("age", 15, 30)
     *          .build();
     *
     *     // 查询 2020 年新注册的用户数
     *     Query query = Query.newBuilder()
     *          .groupField("count(id) as count")
     *          .between("createTime", "2020-01-01 00:00:00", "2020-12-31 23:59:59")
     *          .build();
     * </pre>
     *
     * @param field    字段名
     * @param minValue 最小值. 包含最小值
     * @param maxValue 最大值. 不包含最大值
     */
    public OrFilterBuilder betweenExcludeRight(String field, Object minValue, Object maxValue) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'betweenExcludeRight' cannot be empty");
        }

        if (minValue == null || maxValue == null) {
            throw new IllegalArgumentException("Query: the field[" + field + "] range value [" + minValue + "," + maxValue + "] of condition 'between' cannot be null");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.BETWEEN_EXCLUDE_RIGHT.apply(minValue, maxValue)));

        return this;
    }

    /**
     * 在指定取值范围之内, 左闭右开. 即: [minValue, maxValue).
     *
     * @see #betweenExcludeRight(String, Object, Object)
     */
    public <T> OrFilterBuilder betweenExcludeRight(SFunction<T, ?> column, Object minValue, Object maxValue) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.betweenExcludeRight(propName, minValue, maxValue);
    }


    /**
     * 在指定取值范围之内, 左开右开. 即: (minValue, maxValue).
     *
     * <pre>
     *     例如:
     *     // 查询年龄在 15 - 30 岁之间的用户. 不包括 15 和 30 岁
     *     Query query = Query.newBuilder()
     *          .select(User.class)
     *          .between("age", 15, 30)
     *          .build();
     *
     *     // 查询 2020 年新注册的用户数
     *     Query query = Query.newBuilder()
     *          .groupField("count(id) as count")
     *          .between("createTime", "2020-01-01 00:00:00", "2020-12-31 23:59:59")
     *          .build();
     * </pre>
     *
     * @param field    字段名
     * @param minValue 最小值. 不包含最小值
     * @param maxValue 最大值. 不包含最大值
     */
    public OrFilterBuilder betweenExcludeAll(String field, Object minValue, Object maxValue) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("Query: the field name of condition 'betweenExcludeAll' cannot be empty");
        }

        if (minValue == null || maxValue == null) {
            throw new IllegalArgumentException("Query: the field[" + field + "] range value [" + minValue + "," + maxValue + "] of condition 'between' cannot be null");
        }

        this.filters.add(Collections.singletonMap(field.trim(), LogicOp.BETWEEN_EXCLUDE_ALL.apply(minValue, maxValue)));

        return this;
    }

    /**
     * 在指定取值范围之内, 左开右开. 即: (minValue, maxValue).
     *
     * @see #betweenExcludeAll(String, Object, Object)
     */
    public <T> OrFilterBuilder betweenExcludeAll(SFunction<T, ?> column, Object minValue, Object maxValue) {
        String propName = BuilderUtils.getPropertyName(column);
        return this.betweenExcludeAll(propName, minValue, maxValue);
    }
}
