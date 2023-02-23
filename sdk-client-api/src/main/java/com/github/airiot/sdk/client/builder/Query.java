package com.github.airiot.sdk.client.builder;


import com.github.airiot.sdk.client.annotation.Field;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询请求构建器
 */
public class Query {

    private final static Gson GSON = new GsonBuilder().create();

    private final transient Builder builder;

    /**
     * 要返回的字段列表
     */
    private final Map<String, Object> project;
    /**
     * 筛选条件
     */
    private final Map<String, Object> filter;
    /**
     * 分组字段列表
     */
    private final Map<String, Object> groupBy;
    /**
     * 汇总字段
     */
    private final Map<String, Object> groupFields;
    /**
     * 排序
     */
    private final Map<String, Integer> sort;
    /**
     * 跳过记录数. 通常用于分页查询
     */
    private final Integer skip;
    /**
     * 限制返回的记录数量
     */
    private final Integer limit;
    /**
     * 是否统计匹配的记录行数
     */
    private final Boolean withCount;

    public static class Builder {

        private static final int ASC = 1;
        private static final int DESC = -1;

        private final Set<String> excludeProjects = new HashSet<>();
        private Map<String, Object> projects;
        private Map<String, Object> filters;
        private Map<String, Object> groupBys;
        private Map<String, Object> groupFields;
        private Map<String, Integer> sort;
        private Integer skip;
        private Integer limit;
        private Boolean withCount;

        public boolean containsSelectField(String field) {
            if (CollectionUtils.isEmpty(this.projects)) {
                return false;
            }
            return this.projects.containsKey(field);
        }

        public <T> boolean containsSelectField(SFunction<T, ?> column) {
            return this.containsSelectField(BuilderUtils.getPropertyName(column));
        }

        private void checkOrCreateFilters() {
            if (this.filters != null) {
                return;
            }
            this.filters = new HashMap<>();
        }

        /**
         * @see #select(Collection)
         */
        public Builder select(String... fields) {
            return this.select(Arrays.asList(fields));
        }


        public <T> Builder select(SFunction<T, ?>... columns) {
            if (columns == null || columns.length == 0) {
                return this;
            }

            List<String> columnNames = Arrays.stream(columns).map(BuilderUtils::getPropertyName).collect(Collectors.toCollection(
                    () -> new ArrayList<>(columns.length)
            ));

            return this.select(columnNames);
        }

        /**
         * 获取指定类型中定义的全部字段
         * <br>
         * 如果字段被 static 和 transient 修饰则会跳过. 如果字段上带有 {@link Field} 则使用该注解定义的名称
         *
         * @param tClass 类型
         */
        public <T> Builder select(Class<T> tClass) {
            return this.select(BuilderUtils.getColumns(tClass));
        }

        /**
         * 设置查询返回的字段列表
         *
         * @param fields 字段名称列表
         */
        public Builder select(Collection<String> fields) {
            if (CollectionUtils.isEmpty(fields)) {
                return this;
            }

            if (this.projects == null) {
                this.projects = Maps.newHashMapWithExpectedSize(fields.size());
            }

            for (String field : fields) {
                if (!StringUtils.hasText(field)) {
                    throw new IllegalArgumentException("Query: the select field cannot be empty");
                } else if (this.excludeProjects.contains(field)) {
                    continue;
                }
                this.projects.put(field.trim(), 1);
            }

            return this;
        }


        /**
         * 排除要查询的字段列表, 即查询结果中不返回该字段
         *
         * @param columns 要排除的字段名称列表
         */
        public Builder exclude(Collection<String> columns) {
            if (CollectionUtils.isEmpty(columns) || CollectionUtils.isEmpty(projects)) {
                return this;
            }

            for (String column : columns) {
                this.projects.remove(column);
            }

            this.excludeProjects.addAll(columns);

            return this;
        }


        public Builder exclude(String... columns) {
            if (columns == null) {
                return this;
            }
            return this.exclude(Arrays.asList(columns));
        }

        public <T> Builder exclude(SFunction<T, ?>... columns) {
            if (columns == null) {
                return this;
            }

            List<String> columnNames = Arrays.stream(columns).map(BuilderUtils::getPropertyName).collect(Collectors.toCollection(
                    () -> new ArrayList<>(columns.length)
            ));

            return this.exclude(columnNames);
        }

        protected Builder summary(Map<String, Object> field) {
            if (field == null) {
                throw new IllegalArgumentException("the select 'field' cannot be null or empty");
            }

            if (field.isEmpty()) {
                return this;
            }
            if (this.groupFields == null) {
                this.groupFields = new HashMap<>(1);
            }

            this.groupFields.putAll(field);
            return this;
        }

        /**
         * 汇总查询字段
         *
         * @param field 字段名
         */
        public AggregateBuilder summary(String field) {
            return new AggregateBuilder(this, field);
        }

        /**
         * 汇总查询字段
         *
         * @param column 列
         */
        public <T> AggregateBuilder summary(SFunction<T, ?> column) {
            return this.summary(BuilderUtils.getPropertyName(column));
        }

        /**
         * 分组
         *
         * @param field 字段名
         */
        public GroupByBuilder groupBy(String field) {
            return new GroupByBuilder(this, field);
        }

        /**
         * 分组汇总
         */
        protected Builder groupBy(Map<String, Object> field) {
            if (this.groupBys == null) {
                this.groupBys = new HashMap<>();
            }
            this.groupBys.putAll(field);
            return this;
        }

        /**
         * 分组汇总
         *
         * @param column 列
         */
        public <T> GroupByBuilder groupBy(SFunction<T, ?> column) {
            return this.groupBy(BuilderUtils.getPropertyName(column));
        }

        /**
         * 等于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public <T> Builder eq(String field, T value) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'eq' cannot be empty");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.EQ.apply(value));

            return this;
        }

        public <Type, T> Builder eq(SFunction<Type, ?> column, T value) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.eq(propName, value);
        }

        /**
         * 不等于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public <T> Builder ne(String field, T value) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'ne' cannot be empty");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.NE.apply(value));

            return this;
        }

        public <Type, T> Builder ne(SFunction<Type, ?> column, T value) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.ne(propName, value);
        }

        public <T> Builder in(String field, T... value) {
            return this.in(field, Arrays.asList(value));
        }

        public <Type, T> Builder in(SFunction<Type, ?> column, T value) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.in(propName, value);
        }

        /**
         * 在等指列表之内
         *
         * @param field 字段名
         * @param value 字段值列表
         */
        public <T> Builder in(String field, Collection<T> value) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'in' cannot be empty");
            }

            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("Query: the field[" + field + "] values of condition 'in' cannot be null and  empty");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.IN.apply(value));

            return this;
        }

        public <T> Builder notIn(String field, T... values) {
            if (values == null || values.length == 0) {
                throw new IllegalArgumentException("Query: the field[" + field + "] values of condition 'not in' cannot be null and empty");
            }
            return this.notIn(field, Arrays.asList(values));
        }

        public <Type, T> Builder notIn(SFunction<Type, ?> column, T... values) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.notIn(propName, values);
        }

        /**
         * 不在指定列表之内
         *
         * @param field  字段名
         * @param values 字段值列表
         */
        public <T> Builder notIn(String field, Collection<T> values) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'not in' cannot be empty");
            }

            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("Query: the field[" + field + "] values of condition 'not in' cannot be null and empty");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.NOT_IN.apply(values));

            return this;
        }

        /**
         * 小于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public Builder lt(String field, Object value) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'lt' cannot be empty");
            }

            if (value == null) {
                throw new IllegalArgumentException("Query: the field[" + field + "] value of condition 'lt' cannot be null");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.LT.apply(value));

            return this;
        }

        public <T> Builder lt(SFunction<T, ?> column, Object value) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.lt(propName, value);
        }

        /**
         * 小于或等于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public Builder lte(String field, Object value) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'lte' cannot be empty");
            }

            if (value == null) {
                throw new IllegalArgumentException("Query: the field[" + field + "] value of condition 'lte' cannot be null");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.LTE.apply(value));

            return this;
        }

        public <T> Builder lte(SFunction<T, ?> column, Object value) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.lte(propName, value);
        }

        /**
         * 大于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public Builder gt(String field, Object value) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'gt' cannot be empty");
            }

            if (value == null) {
                throw new IllegalArgumentException("Query: the field[" + field + "] value of condition 'gt' cannot be null");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.GT.apply(value));

            return this;
        }

        public <T> Builder gt(SFunction<T, ?> column, Object value) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.gt(propName, value);
        }

        /**
         * 大于或等于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public Builder gte(String field, Object value) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'gte' cannot be empty");
            }

            if (value == null) {
                throw new IllegalArgumentException("Query: the field[" + field + "] value of condition 'gte' cannot be null");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.GTE.apply(value));

            return this;
        }

        public <T> Builder gte(SFunction<T, ?> column, Object value) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.gte(propName, value);
        }

        /**
         * 在指定取值范围之内. [minValue, maxValue).
         *
         * <br>
         * 注: 取值范围左闭右开.
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
         *          .between("createTime", "2020-01-01 00:00:00", "2021-01-01 00:00:00")
         *          .build();
         * </pre>
         *
         * @param field    字段名
         * @param minValue 最小值. 包含最小值
         * @param maxValue 最大值. 不含最大值
         */
        public Builder between(String field, Object minValue, Object maxValue) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'gte' cannot be empty");
            }

            if (minValue == null || maxValue == null) {
                throw new IllegalArgumentException("Query: the field[" + field + "] range value [" + minValue + "," + maxValue + "] of condition 'between' cannot be null");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.BETWEEN.apply(minValue, maxValue));

            return this;
        }

        public <T> Builder between(SFunction<T, ?> column, Object minValue, Object maxValue) {
            String propName = BuilderUtils.getPropertyName(column);
            return this.between(propName, minValue, maxValue);
        }

        /**
         * 添加升序字段
         *
         * @param fields 字段名称列表
         */
        public Builder orderAsc(Collection<String> fields) {
            if (CollectionUtils.isEmpty(fields)) {
                return this;
            }

            if (this.sort == null) {
                this.sort = Maps.newHashMapWithExpectedSize(fields.size());
            }

            for (String field : fields) {
                if (!StringUtils.hasText(field)) {
                    throw new IllegalArgumentException("Query: the field name of sortAsc cannot be empty");
                }
                this.sort.put(field.trim(), ASC);
            }
            return this;
        }

        /**
         * @see #orderAsc(Collection)
         */
        public Builder orderAsc(String... fields) {
            if (fields == null || fields.length == 0) {
                return this;
            }
            return this.orderAsc(Arrays.asList(fields));
        }

        public <T> Builder orderAsc(SFunction<T, ?>... fields) {
            if (fields == null || fields.length == 0) {
                return this;
            }

            List<String> columns = Arrays.stream(fields)
                    .map(BuilderUtils::getPropertyName).collect(Collectors.toCollection(() -> new ArrayList<>(fields.length)));

            return this.orderAsc(columns);
        }


        /**
         * @see #orderDesc(Collection)
         */
        public Builder orderDesc(String... fields) {
            if (fields == null || fields.length == 0) {
                return this;
            }
            return this.orderDesc(Arrays.asList(fields));
        }

        public <T> Builder orderDesc(SFunction<T, ?>... fields) {
            if (fields == null || fields.length == 0) {
                return this;
            }

            List<String> columns = Arrays.stream(fields)
                    .map(BuilderUtils::getPropertyName).collect(Collectors.toCollection(() -> new ArrayList<>(fields.length)));

            return this.orderDesc(columns);
        }

        /**
         * 添加降序字段
         *
         * @param fields 字段名称列表
         */
        public Builder orderDesc(Collection<String> fields) {
            if (CollectionUtils.isEmpty(fields)) {
                return this;
            }

            if (this.sort == null) {
                this.sort = Maps.newHashMapWithExpectedSize(fields.size());
            }

            for (String field : fields) {
                if (!StringUtils.hasText(field)) {
                    throw new IllegalArgumentException("Query: the field name of sortDesc cannot be empty");
                }
                this.sort.put(field.trim(), DESC);
            }

            return this;
        }

        /**
         * 设置跳过的记录数, 用于分页
         *
         * @param skip 跳过数量
         */
        public Builder skip(int skip) {
            if (skip < 0) {
                throw new IllegalArgumentException("Query: skip cannot less than 0");
            }
            this.skip = skip;
            return this;
        }

        /**
         * 设置返回的记录数
         *
         * @param limit 返回的记录数量
         */
        public Builder limit(int limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("Query: limit cannot less than 0");
            }
            this.limit = limit;
            return this;
        }

        /**
         * 返回总记录数
         */
        public Builder withCount() {
            this.withCount = true;
            return this;
        }

        public Query build() {
            return new Query(this.projects, this.filters,
                    this.groupBys, this.groupFields, this.sort,
                    this.skip, this.limit, this.withCount,
                    this);
        }
    }

    private Query(Map<String, Object> project, Map<String, Object> filter,
                  Map<String, Object> groupBy, Map<String, Object> groupFields,
                  Map<String, Integer> sort, Integer skip, Integer limit,
                  Boolean withCount, Builder builder) {
        this.project = project;
        this.filter = filter;
        this.groupBy = groupBy;
        this.groupFields = groupFields;
        this.sort = sort;
        this.skip = skip;
        this.limit = limit;
        this.withCount = withCount;
        this.builder = builder;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return this.builder;
    }

    /**
     * 判断是否定义了查询字段
     *
     * @return 如果未定义则返回 {@code false}
     */
    public boolean hasSelectFields() {
        return !CollectionUtils.isEmpty(this.project);
    }

    /**
     * 判断是否定义了筛选条件
     * @return 如果未定义则返回 {@code false}
     */
    public boolean hasFilters() {
        return !CollectionUtils.isEmpty(this.filter);
    }

    public byte[] serialize() {
        return GSON.toJson(this).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] serializeFilter() {
        return GSON.toJson(this.filter).getBytes(StandardCharsets.UTF_8);
    }
}
