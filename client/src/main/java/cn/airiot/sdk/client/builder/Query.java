package cn.airiot.sdk.client.builder;


import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 查询请求构建器
 */
public class Query {

    private final static Gson GSON = new GsonBuilder().create();

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

        private Map<String, Object> projects;
        private Map<String, Object> filters;
        private Map<String, Object> groupBys;
        private Map<String, Object> groupFields;
        private Map<String, Integer> sort;
        private Integer skip;
        private Integer limit;
        private Boolean withCount;

        /**
         * @see #select(Collection)
         */
        public Builder select(String... fields) {
            return this.select(Arrays.asList(fields));
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
                }

                this.projects.put(field.trim(), 1);
            }

            return this;
        }

        private void checkOrCreateFilters() {
            if (this.filters != null) {
                return;
            }
            this.filters = new HashMap<>();
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

        public <T> Builder in(String field, T... value) {
            return this.in(field, Arrays.asList(value));
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


        public <T> Builder notIn(String field, T... value) {
            return this.notIn(field, Arrays.asList(value));
        }

        /**
         * 不在指定列表之内
         *
         * @param field 字段名
         * @param value 字段值列表
         */
        public <T> Builder notIn(String field, Collection<T> value) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the field name of condition 'not in' cannot be empty");
            }

            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("Query: the field[" + field + "] values of condition 'not in' cannot be null and empty");
            }

            this.checkOrCreateFilters();

            this.filters.put(field.trim(), LogicOp.NIN.apply(value));

            return this;
        }

        /**
         * 小于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public <T extends Number> Builder lt(String field, T value) {
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

        /**
         * 小于或等于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public <T extends Number> Builder lte(String field, T value) {
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

        /**
         * 大于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public <T extends Number> Builder gt(String field, T value) {
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

        /**
         * 大于或等于
         *
         * @param field 字段名
         * @param value 字段值
         */
        public <T extends Number> Builder gte(String field, T value) {
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


        /**
         * @see #orderDesc(Collection)
         */
        public Builder orderDesc(String... fields) {
            if (fields == null || fields.length == 0) {
                return this;
            }
            return this.orderDesc(Arrays.asList(fields));
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
            return new Query(this.projects, this.filters, this.groupBys, this.groupFields, this.sort, this.skip, this.limit, this.withCount);
        }
    }

    private Query(Map<String, Object> project, Map<String, Object> filter,
                  Map<String, Object> groupBy, Map<String, Object> groupFields,
                  Map<String, Integer> sort, Integer skip, Integer limit,
                  Boolean withCount) {
        this.project = project;
        this.filter = filter;
        this.groupBy = groupBy;
        this.groupFields = groupFields;
        this.sort = sort;
        this.skip = skip;
        this.limit = limit;
        this.withCount = withCount;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public byte[] toBytes() {
        return this.toBytes(StandardCharsets.UTF_8);
    }

    public byte[] toBytes(Charset charset) {
        return GSON.toJson(this).getBytes(charset);
    }
}
