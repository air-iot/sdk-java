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

package io.github.airiot.sdk.client.service.core.dto.timing;

import com.google.common.collect.Maps;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 时序数据查询结果
 */
public class TimingDataSeries {

    /**
     * 表名
     */
    private final String name;
    /**
     * 标签信息.
     * 当 group 进行分组字段中包含除 time 之外的标签时, 该字段会有相关标签的值.
     * <pre>
     *
     *     TimingDataQuery.newBuilder()
     *                 .select("b")
     *                 .selectAs("max(\"a\")", "FA")
     *                 .table("tcp_client")
     *                 .timeBetween(LocalDateTime.now().minusDays(10), LocalDateTime.now())
     *                 .node("tcp_client_002")
     *                 .groupByNode()
     *                 .groupByTime("5m")
     *                 .fill("-100")
     *                 .finish()
     *                 .build();
     *    以上查询中, 使用了 groupByNode() 函数即按资产编号进行分组, 此时查询结果中每个资产对应一个 TimingDataSeries 对象,
     *    TimingDataSeries#tags 字段中会有 "id" 标签信息, 值为资产编号, 可通过标签信息区分 TimingDataSeries 所属的资产
     *
     * </pre>
     */
    private final Map<String, String> tags;
    /**
     * 查询返回的字段列表
     */
    private final List<String> columns;
    /**
     * 结果数据, 每个元素代码一行记录
     */
    private final List<Value> values;

    public String getName() {
        return name;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Optional<String> getTag(String tagName) {
        return Optional.ofNullable(this.tags.get(tagName));
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Value> getValues() {
        return values;
    }

    public TimingDataSeries(String name, Map<String, String> tags, List<String> columns, List<Value> values) {
        this.name = name;
        this.tags = tags;
        this.columns = columns;
        this.values = values;
    }

    public static class Value {

        private final ZonedDateTime time;
        private final Map<String, Object> values;

        /**
         * 获取数据的时间
         */
        public ZonedDateTime getTime() {
            return time;
        }

        /**
         * 获取所有字段的值
         */
        public Map<String, Object> getValues() {
            return this.values;
        }

        /**
         * 获取指定字段的值
         *
         * @param column 字段名
         */
        public Object getValue(String column) {
            return this.values.get(column);
        }

        public Value(List<String> columns, List<Object> values) {
            this.time = ZonedDateTime.parse(values.get(0).toString());
            this.values = Maps.newHashMapWithExpectedSize(columns.size() - 1);
            for (int i = 1; i < columns.size(); i++) {
                this.values.put(columns.get(i), values.get(i));
            }
        }
    }
}
