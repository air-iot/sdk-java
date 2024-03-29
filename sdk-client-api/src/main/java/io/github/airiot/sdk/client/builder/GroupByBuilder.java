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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GroupByBuilder {

    /**
     * 按分钟汇总
     */
    public static final String DATE_FORMAT_OF_MINUTE = "%Y-%m-%dT%H:%M:00";
    /**
     * 按小时汇总
     */
    public static final String DATE_FORMAT_OF_HOUR = "%Y-%m-%dT%H:00:00";
    /**
     * 按天汇总
     */
    public static final String DATE_FORMAT_OF_DAY = "%Y-%m-%d";
    /**
     * 按周汇总
     */
    public static final String DATE_FORMAT_OF_WEEK = "%Y-%V";
    /**
     * 按月汇总
     */
    public static final String DATE_FORMAT_OF_MONTH = "%Y-%m";
    /**
     * 按年汇总
     */
    public static final String DATE_FORMAT_OF_YEAR = "%Y";

    private final Query.Builder builder;
    private final String field;

    public GroupByBuilder(Query.Builder builder, String field) {
        this.builder = builder;
        this.field = field;
    }
    
    /**
     * 无别名. 即别名与字段名一致
     */
    public Query.Builder sameToField() {
        return this.builder.groupBy(Collections.singletonMap(this.field, String.format("$%s", this.field)));
    }

    /**
     * 定义别名
     *
     * @param alias 别名
     */
    public Query.Builder alias(String alias) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the alias of group by '" + this.field + "' cannot be null or empty");
        }

        return this.builder.groupBy(Collections.singletonMap(alias, String.format("$%s", this.field)));
    }

    /**
     * 日期格式化
     *
     * @param alias  字段别名
     * @param format 日期格式
     */
    public Query.Builder dateFormat(String alias, String format) {
        if (!StringUtils.hasText(alias)) {
            throw new IllegalArgumentException("the alias of group by '" + this.field + "' cannot be null or empty");
        }

        if (!StringUtils.hasText(format)) {
            throw new IllegalArgumentException("the format of group by '" + this.field + "' cannot be null or empty");
        }

        Map<String, Object> props = new HashMap<>();
        props.put("format", format);
        props.put("date", String.format("$%s", this.field));

        return this.builder.groupBy(
                Collections.singletonMap(alias,
                        Collections.singletonMap("$dateToString", props))
        );
    }
}
