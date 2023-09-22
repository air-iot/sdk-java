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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.airiot.sdk.client.annotation.Field;
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

        public Set<String> getExcludeProjects() {
            return excludeProjects;
        }

        public Map<String, Object> getProjects() {
            return projects;
        }

        public void setProjects(Map<String, Object> projects) {
            this.projects = projects;
        }

        public Map<String, Object> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, Object> filters) {
            this.filters = filters;
        }

        public Map<String, Object> getGroupBys() {
            return groupBys;
        }

        public void setGroupBys(Map<String, Object> groupBys) {
            this.groupBys = groupBys;
        }

        public Map<String, Object> getGroupFields() {
            return groupFields;
        }

        public void setGroupFields(Map<String, Object> groupFields) {
            this.groupFields = groupFields;
        }

        public Map<String, Integer> getSort() {
            return sort;
        }

        public void setSort(Map<String, Integer> sort) {
            this.sort = sort;
        }

        public Integer getSkip() {
            return skip;
        }

        public void setSkip(Integer skip) {
            this.skip = skip;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Boolean getWithCount() {
            return withCount;
        }

        public void setWithCount(Boolean withCount) {
            this.withCount = withCount;
        }

        public FilterBuilder filter() {
            if (this.filters == null) {
                this.filters = new HashMap<>();
            }
            return new FilterBuilder(this, this.filters);
        }
        
        public OrFilterBuilder or() {
            List<Map<String, Object>> orFilters = null;
            if (this.filters == null) {
                this.filters = new HashMap<>();
            } else {
                orFilters = (List<Map<String, Object>>) this.filters.get("$or");
            }

            if (orFilters == null) {
                orFilters = new ArrayList<>();
                this.filters.put("$or", orFilters);
            }
            return new OrFilterBuilder(this, orFilters);
        }

        /**
         * 判断现有查询字段列表中是否包含指定字段
         *
         * @param field 字段名称
         * @return 如果已经包含则返回 true, 否则返回 false
         */
        public boolean containsSelectField(String field) {
            if (CollectionUtils.isEmpty(this.projects)) {
                return false;
            }
            return this.projects.containsKey(field);
        }

        /**
         * 判断现有查询字段列表中是否包含指定字段
         *
         * @param column 字段名称
         * @return 如果已经包含则返回 true, 否则返回 false
         */
        public <T> boolean containsSelectField(SFunction<T, ?> column) {
            return this.containsSelectField(BuilderUtils.getPropertyName(column));
        }

        /**
         * 添加要返回的字段列表
         *
         * @see #select(Collection)
         */
        public Builder select(String... fields) {
            return this.select(Arrays.asList(fields));
        }

        /**
         * 添加要返回的字段列表
         *
         * @see #select(Collection)
         */
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
         * 设置查询返回的嵌套对象内的字段列表
         *
         * @see #selectSubFields(String, Map)
         */
        public <T> Builder selectSubFields(SFunction<T, ?> column, String... subFields) {
            if (subFields == null || subFields.length == 0) {
                throw new IllegalArgumentException("Query: the select sub fields cannot be empty");
            }
            return this.selectSubFields(BuilderUtils.getPropertyName(column), Arrays.stream(subFields).collect(Collectors.toMap(subField -> subField, v -> 1)));
        }

        /**
         * 设置查询返回的嵌套对象内的字段列表
         *
         * @see #selectSubFields(String, Map)
         */
        public <T> Builder selectSubFields(String field, String... subFields) {
            if (subFields == null || subFields.length == 0) {
                throw new IllegalArgumentException("Query: the select sub fields cannot be empty");
            }

            return this.selectSubFields(field, Arrays.stream(subFields).collect(Collectors.toMap(subField -> subField, v -> 1)));
        }

        /**
         * 设置查询返回的嵌套对象内的字段列表
         *
         * @see #selectSubFields(String, Map)
         */
        public <T> Builder selectSubFields(SFunction<T, ?> column, Map<String, Object> subFields) {
            return this.selectSubFields(BuilderUtils.getPropertyName(column), subFields);
        }

        /**
         * 设置查询返回的嵌套对象内的字段列表
         *
         * @param field     字段名称
         * @param subFields 嵌套对象内的字段列表
         */
        public Builder selectSubFields(String field, Map<String, Object> subFields) {
            if (!StringUtils.hasText(field)) {
                throw new IllegalArgumentException("Query: the select field cannot be empty");
            }

            if (CollectionUtils.isEmpty(subFields)) {
                return this;
            }

            if (this.projects == null) {
                this.projects = Maps.newHashMapWithExpectedSize(1);
                this.projects.put(field, subFields);
                return this;
            }

            Object projectField = this.projects.get(field);
            if (projectField == null) {
                this.projects.put(field, subFields);
            } else if (projectField instanceof Map) {
                ((Map<String, Object>) projectField).putAll(subFields);
            } else {
                throw new IllegalArgumentException("Query: the select field '" + field + "' has been set to a non-nested object");
            }

            return this;
        }

        /**
         * 排除要查询的字段列表, 即查询结果中不返回该字段
         * <br>
         * 通常情况下, 在使用 {@link #select(Class)} 添加所有字段后, 再使用该方法排除不需要的字段
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


        /**
         * 排除要查询的字段列表
         *
         * @see #exclude(Collection)
         */
        public Builder exclude(String... columns) {
            if (columns == null) {
                return this;
            }
            return this.exclude(Arrays.asList(columns));
        }

        /**
         * 排除要查询的字段列表
         *
         * @see #exclude(Collection)
         */
        public <T> Builder exclude(SFunction<T, ?>... columns) {
            if (columns == null) {
                return this;
            }

            List<String> columnNames = Arrays.stream(columns).map(BuilderUtils::getPropertyName).collect(Collectors.toCollection(
                    () -> new ArrayList<>(columns.length)
            ));

            return this.exclude(columnNames);
        }

        /**
         * 汇总查询
         *
         * <pre>
         *     示例1: 统计匹配行数
         *     {
         *         "count": {
         *             "$sum": 1
         *         }
         *     }
         *
         *     示例2: 最小值
         *     {
         *         "minValue": {
         *             "$min": "$value"
         *         }
         *     }
         *
         *     示例3: 求和
         *     {
         *         "sum": {
         *             "$sum": "$value"
         *         }
         *     }
         * </pre>
         *
         * @param field 汇总字段及汇总方式
         */
        protected Builder summary(Map<String, Object> field) {
            if (field == null || field.isEmpty()) {
                throw new IllegalArgumentException("the select 'field' cannot be null or empty");
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
         * 添加升序字段
         *
         * @see #orderAsc(Collection)
         */
        public Builder orderAsc(String... fields) {
            if (fields == null || fields.length == 0) {
                return this;
            }
            return this.orderAsc(Arrays.asList(fields));
        }

        /**
         * 添加升序字段
         *
         * @see #orderAsc(Collection)
         */
        public <T> Builder orderAsc(SFunction<T, ?>... fields) {
            if (fields == null || fields.length == 0) {
                return this;
            }

            List<String> columns = Arrays.stream(fields)
                    .map(BuilderUtils::getPropertyName).collect(Collectors.toCollection(() -> new ArrayList<>(fields.length)));

            return this.orderAsc(columns);
        }


        /**
         * 添加降序字段
         *
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
         * @see #orderDesc(Collection)
         */
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
     *
     * @return 如果未定义则返回 {@code false}
     */
    public boolean hasFilters() {
        return !CollectionUtils.isEmpty(this.filter);
    }

    /**
     * 查询返回的字段列表
     *
     * @return 字段列表
     */
    public Map<String, ?> getProject() {
        return this.project;
    }

    /**
     * 查询条件信息
     *
     * @return 查询条件信息
     */
    public Map<String, ?> getFilters() {
        return this.filter;
    }

    public Map<String, Integer> getSort() {
        return this.sort;
    }

    public int getLimit() {
        return this.limit;
    }

    public int getSkip() {
        return this.skip;
    }

    public boolean isWithCount() {
        return this.withCount;
    }

    /**
     * 将查询对象序列化为字节数组
     *
     * @return 序列化后的字节数组
     */
    public byte[] serialize() {
        return GSON.toJson(this).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 将查询对象序列化为字符串
     *
     * @return 序列化后的字符串
     */
    public String serializeToString() {
        return GSON.toJson(this);
    }

    /**
     * 将查询条件信息序列化为字节数组
     *
     * @return 序列化后的字节数组
     */
    public byte[] serializeFilter() {
        return GSON.toJson(this.filter).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 将查询条件信息序列化为字符串
     *
     * @return 序列化后的字符串
     */
    public String serializeFilterToString() {
        return GSON.toJson(this.filter);
    }
}
