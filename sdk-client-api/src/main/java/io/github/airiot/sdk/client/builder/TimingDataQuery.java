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


import com.google.gson.annotations.SerializedName;
import io.github.airiot.sdk.client.Constants;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 时序数据查询
 *
 * <pre>{@code
 * [{
 * 	"fields": [
 * 		"MEAN(\"tagid3\") as XXX",
 * 		"MEAN(\"\tagid4\") as YYY"
 * 	],
 * 	"id": "adg",
 * 	"tableId": "5f4db17d95b6b5c728dfadd3",
 * 	"where": [
 * 		"time >= '2019-04-15 00:00:00'",
 * 		"time <= '2019-04-16 00:00:00'"
 * 	],
 * 	"deartment": ["5cceba2cc7157e23ae172c28"],
 * 	"group": [
 * 		"time(5m)"
 * 	],
 * 	"fill": "0",
 * 	"order": "time asc",
 * 	"limit": 5,
 * 	"offset": 0
 * }]}
 * </pre>
 *
 * @see Constants 相关常量
 */
public class TimingDataQuery {

    /**
     * 查询的字段列表
     */
    private List<String> fields;
    /**
     * 模型ID(工作表标识)
     */
    private String tableId;
    /**
     * 资产编号
     */
    @SerializedName(Constants.TIMING_NODE_ID)
    private String nodeId;
    /**
     * 部门ID列表
     */
    @SerializedName("deartment")
    private List<String> departments;
    /**
     * 过滤条件
     */
    private List<String> where;
    /**
     * 分组
     */
    private List<String> group;

    /**
     * 分组查询时, 数据缺失时的填充值
     */
    private String fill;
    /**
     * 排序条件
     */
    private String order;
    /**
     * 返回数据的条数
     */
    private Integer limit;
    /**
     * 返回数据的起始位置
     */
    private Integer offset;

    public static class Builder {

        public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        private List<TimingDataQuery> queries = new ArrayList<>(1);

        /**
         * 查询的字段列表
         */
        private List<String> fields = new ArrayList<>();
        /**
         * 模型ID(工作表标识)
         */
        private String tableId;
        /**
         * 资产编号
         */
        private String nodeId;
        /**
         * 部门ID列表
         */
        private Set<String> departments = new HashSet<>();
        /**
         * 过滤条件
         */
        private LinkedHashMap<String, Map<LogicOp, Object>> where = new LinkedHashMap<>();
        /**
         * 分组
         */
        private List<String> group = new ArrayList<>();
        /**
         * 分组查询时, 数据缺失时的填充值
         */
        private String fill;
        /**
         * 排序条件
         */
        private LinkedHashMap<String, String> order = new LinkedHashMap<>();
        /**
         * 返回数据的条数
         */
        private Integer limit;
        /**
         * 返回数据的起始位置
         */
        private Integer offset;

        /**
         * 查询的字段列表
         *
         * <pre>
         *     例如: select("temperature", "avg(\"humidity\")") 为查询温度值和湿度的平均值
         * </pre>
         *
         * @param fields 字段列表
         */
        public Builder select(String... fields) {
            if (fields == null || fields.length == 0) {
                throw new IllegalArgumentException("the select fields can not be empty");
            }
            this.fields.addAll(Arrays.asList(fields));
            return this;
        }

        /**
         * 查询字段并自定义别名
         * <br>
         * selectAs("max(\"temp\")", "maxTemp") 等同于 fields: ["max(\"temp\") as maxTemp"]
         *
         * @param field 字段
         * @param alias 别名
         */
        public Builder selectAs(String field, String alias) {
            this.fields.add(String.format("%s as %s", field, alias));
            return this;
        }

        /**
         * 要查询的数据所在表(工作表标识)
         *
         * @param tableId 工作表标识
         */
        public Builder table(String tableId) {
            if (!StringUtils.hasText(tableId)) {
                throw new IllegalArgumentException("the tableId cannot be empty");
            }

            this.tableId = tableId.trim();
            return this;
        }

        /**
         * 要查询的资产编号
         *
         * @param nodeId 资产编号
         */
        public Builder node(String nodeId) {
            if (!StringUtils.hasText(nodeId)) {
                throw new IllegalArgumentException("the nodeId cannot be empty");
            }
            this.nodeId = nodeId;
            return this;
        }

        /**
         * 查询指定部门内资产的数据
         *
         * @param departments 部门ID列表
         */
        public Builder department(String... departments) {
            if (departments == null || departments.length == 0) {
                throw new IllegalArgumentException("the departments cannot be empty");
            }

            this.departments.addAll(Arrays.asList(departments));
            return this;
        }

        /**
         * 查询指定部门内资产的数据
         *
         * @param departments 部门ID列表
         */
        public Builder departments(Collection<String> departments) {
            if (departments == null || departments.isEmpty()) {
                throw new IllegalArgumentException("the departments cannot be empty");
            }

            this.departments.addAll(departments);
            return this;
        }

        /**
         * 等于
         *
         * @param tag   标签名
         * @param value 匹配的值
         */
        public Builder eq(String tag, Object value) {
            if (!StringUtils.hasText(tag)) {
                throw new IllegalArgumentException("the tag of where cause can not be empty");
            }

            this.where.put(tag, Collections.singletonMap(LogicOp.EQ, value));
            return this;
        }

        /**
         * 不等于
         *
         * @param tag   标签名
         * @param value 匹配的值
         */
        public Builder notEq(String tag, Object value) {
            if (!StringUtils.hasText(tag)) {
                throw new IllegalArgumentException("the tag of where cause can not be empty");
            }
            this.where.put(tag, Collections.singletonMap(LogicOp.NE, value));
            return this;
        }

        /**
         * 小于
         *
         * @param tag   标签名
         * @param value 匹配的值
         */
        public Builder lt(String tag, Object value) {
            if (!StringUtils.hasText(tag)) {
                throw new IllegalArgumentException("the tag of where cause can not be empty");
            }
            this.where.put(tag, Collections.singletonMap(LogicOp.LT, value));
            return this;
        }

        /**
         * 小于或等于
         *
         * @param tag   标签名
         * @param value 匹配的值
         */
        public Builder lte(String tag, Object value) {
            if (!StringUtils.hasText(tag)) {
                throw new IllegalArgumentException("the tag of where cause can not be empty");
            }
            this.where.put(tag, Collections.singletonMap(LogicOp.LTE, value));
            return this;
        }

        /**
         * 大于
         *
         * @param tag   标签名
         * @param value 匹配的值
         */
        public Builder gt(String tag, Object value) {
            if (!StringUtils.hasText(tag)) {
                throw new IllegalArgumentException("the tag of where cause can not be empty");
            }
            this.where.put(tag, Collections.singletonMap(LogicOp.GT, value));
            return this;
        }

        /**
         * 大于或等于
         *
         * @param tag   标签名
         * @param value 匹配的值
         */
        public Builder gte(String tag, Object value) {
            if (!StringUtils.hasText(tag)) {
                throw new IllegalArgumentException("the tag of where cause can not be empty");
            }
            this.where.put(tag, Collections.singletonMap(LogicOp.GTE, value));
            return this;
        }

        /**
         * 时间范围
         *
         * @param startTime 开始时间
         * @param endTime   结束时间
         */
        public Builder timeBetween(LocalDateTime startTime, LocalDateTime endTime) {
            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException("the start time and end time can not be empty");
            }

            Map<LogicOp, Object> causes = this.where.getOrDefault("time", new HashMap<>(3));
            startTime.toInstant(ZoneOffset.UTC);

            causes.put(LogicOp.GTE, startTime.format(DATE_TIME_FORMATTER));
            causes.put(LogicOp.LTE, endTime.format(DATE_TIME_FORMATTER));

            this.where.put("time", causes);

            return this;
        }

        /**
         * 查询指定时间之后的数据
         *
         * @param startTime 开始时间
         */
        public Builder startTime(LocalDateTime startTime) {
            if (startTime == null) {
                throw new IllegalArgumentException("the start time can not be empty");
            }

            Map<LogicOp, Object> causes = this.where.getOrDefault("time", new HashMap<>(3));
            causes.put(LogicOp.GTE, startTime.format(DATE_TIME_FORMATTER));
            this.where.put("time", causes);

            return this;
        }

        /**
         * 查询指定时间之前的数据
         *
         * @param endTime 结束时间
         */
        public Builder endTime(LocalDateTime endTime) {
            if (endTime == null) {
                throw new IllegalArgumentException("the end time can not be empty");
            }

            Map<LogicOp, Object> causes = this.where.getOrDefault("time", new HashMap<>(3));
            causes.put(LogicOp.LTE, endTime.format(DATE_TIME_FORMATTER));
            this.where.put("time", causes);

            return this;
        }

        /**
         * 分组
         * <pre>
         *
         * </pre>
         *
         * @param column 分组的字段
         */
        public Builder groupBy(String column) {
            if (!StringUtils.hasText(column)) {
                throw new IllegalArgumentException("the group field cannot be empty");
            }
            this.group.add(column);
            return this;
        }

        /**
         * 按设备进行分组
         */
        public Builder groupByNode() {
            this.group.add(Constants.TIMING_NODE_ID);
            return this;
        }

        /**
         * 按时间进行分组
         * <br>
         * <pre>
         *     groupByTime("5m") 分组间隔为 5 分钟
         *     groupByTime("1d") 分组间隔为 1 天
         * </pre>
         *
         * @param interval 时间间隔. 例如: 5m, 1h, 7d
         */
        public Builder groupByTime(String interval) {
            if (!StringUtils.hasText(interval)) {
                throw new IllegalArgumentException("the interval of time group cannot be empty");
            }
            this.group.add(String.format("time(%s)", interval));
            return this;
        }

        /**
         * 分组后数据缺时填充的数据
         *
         * @param value 填充的数据
         */
        public Builder fill(String value) {
            if (value == null) {
                return this;
            }

            this.fill = value;
            return this;
        }

        /**
         * 按时间降序排序
         */
        public Builder orderByTimeDesc() {
            this.order.remove("time");
            this.order.put("time", "desc");
            return this;
        }

        /**
         * 按时间升序排序
         */
        public Builder orderByTimeAsc() {
            this.order.remove("time");
            this.order.put("time", "asc");
            return this;
        }

        /**
         * 按指定字段升序排序
         *
         * @param field 字段名
         */
        public Builder orderByAsc(String field) {
            this.order.remove(field);
            this.order.put(field, "asc");
            return this;
        }

        /**
         * 按指定字段降序排序
         *
         * @param field 字段名
         */
        public Builder orderByDesc(String field) {
            this.order.remove(field);
            this.order.put(field, "desc");
            return this;
        }

        /**
         * 查询返回的记录数量
         *
         * @param limit 记录数量
         */
        public Builder limit(int limit) {
            if (limit <= 0) {
                throw new IllegalArgumentException("the limit must be rather than 0");
            }

            this.limit = limit;
            return this;
        }

        /**
         * 返回结果的偏移量
         *
         * @param offset 偏移量
         */
        public Builder offset(int offset) {
            if (offset <= 0) {
                throw new IllegalArgumentException("the offset must be rather than 0");
            }
            this.offset = offset;
            return this;
        }

        public Builder finish() {
            TimingDataQuery query = new TimingDataQuery();
            query.fields = new ArrayList<>(this.fields);

            if (StringUtils.hasText(this.tableId)) {
                query.tableId = this.tableId;
            }
            if (StringUtils.hasText(this.nodeId)) {
                query.nodeId = this.nodeId;
            }

            if (!CollectionUtils.isEmpty(this.departments)) {
                query.departments = new ArrayList<>(this.departments);
            }

            if (StringUtils.hasText(this.fill)) {
                query.fill = this.fill;
            }

            if (this.limit != null) {
                query.limit = this.limit;
            }
            if (this.offset != null) {
                query.limit = this.limit;
            }

            if (!this.group.isEmpty()) {
                query.group = new ArrayList<>(this.group);
            }

            if (!this.order.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : this.order.entrySet()) {
                    sb.append(entry.getKey()).append(" ").append(entry.getValue()).append(" ");
                }

                query.order = sb.toString();
            }

            if (!CollectionUtils.isEmpty(this.where)) {
                List<String> whereCauses = new ArrayList<>();
                for (Map.Entry<String, Map<LogicOp, Object>> causes : this.where.entrySet()) {
                    String field = causes.getKey();
                    for (Map.Entry<LogicOp, Object> entry : causes.getValue().entrySet()) {
                        Object value = entry.getValue() instanceof String ? "'" + entry.getValue() + "'" : entry.getValue();
                        switch (entry.getKey()) {
                            case EQ:
                                whereCauses.add(String.format("%s = %s", field, value));
                                break;
                            case NE:
                                whereCauses.add(String.format("%s != %s", field, value));
                                break;
                            case GT:
                                whereCauses.add(String.format("%s > %s", field, value));
                                break;
                            case GTE:
                                whereCauses.add(String.format("%s >= %s", field, value));
                                break;
                            case LT:
                                whereCauses.add(String.format("%s < %s", field, value));
                                break;
                            case LTE:
                                whereCauses.add(String.format("%s <= %s", field, value));
                                break;
                            default:
                                throw new IllegalStateException("不支持的逻辑运算符: " + entry.getKey());
                        }
                    }
                }

                query.where = whereCauses;
            }

            this.queries.add(query);

            this.fields.clear();
            this.departments.clear();
            this.where.clear();
            this.group.clear();
            this.order.clear();

            this.tableId = null;
            this.nodeId = null;

            this.fill = null;

            this.limit = null;
            this.offset = null;

            return this;
        }

        public List<TimingDataQuery> build() {
            return this.queries;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}
