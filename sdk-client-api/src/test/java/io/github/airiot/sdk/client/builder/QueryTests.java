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


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.github.airiot.sdk.client.builder.MapUtils.entry;
import static io.github.airiot.sdk.client.builder.MapUtils.from;

public class QueryTests {

    @Test
    void testEquals() {
        Map<String, Object> filters = Query.newBuilder()
                .eq("id", 123L)
                .eq("name", "小明")
                .eq("age", (short) 18)
                .eq("address", "光明路1号院")
                .eq("isMale", true)
                .eq("birthday", "2019-01-01")
                .getFilters();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("id", 123L);
        expectFilters.put("name", "小明");
        expectFilters.put("age", (short) 18);
        expectFilters.put("address", "光明路1号院");
        expectFilters.put("isMale", true);
        expectFilters.put("birthday", "2019-01-01");

        Assertions.assertThat(expectFilters).isEqualTo(filters);
    }

    @Test
    void testNotEquals() {
        Map<String, Object> filters = Query.newBuilder()
                .ne("id", 123L)
                .ne("name", "小明")
                .ne("age", (short) 18)
                .ne("address", "光明路1号院")
                .ne("isMale", true)
                .ne("birthday", "2019-01-01")
                .getFilters();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("id", Collections.singletonMap("$not", 123L));
        expectFilters.put("name", Collections.singletonMap("$not", "小明"));
        expectFilters.put("age", Collections.singletonMap("$not", (short) 18));
        expectFilters.put("address", Collections.singletonMap("$not", "光明路1号院"));
        expectFilters.put("isMale", Collections.singletonMap("$not", true));
        expectFilters.put("birthday", Collections.singletonMap("$not", "2019-01-01"));

        Assertions.assertThat(expectFilters).isEqualTo(filters);
    }

    @Test
    void testIn() {
        Map<String, Object> filters = Query.newBuilder()
                .in("id", 1)
                .in("age", Arrays.asList(11L, 111L, 1111L))
                .in("name", "小明", "小红")
                .getFilters();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("id", Collections.singletonMap("$in", Collections.singletonList(1)));
        expectFilters.put("age", Collections.singletonMap("$in", Arrays.asList(11L, 111L, 1111L)));
        expectFilters.put("name", Collections.singletonMap("$in", Arrays.asList("小明", "小红")));

        Assertions.assertThat(expectFilters).isEqualTo(filters);
    }

    @Test
    void testNotIn() {
        Map<String, Object> filters = Query.newBuilder()
                .notIn("id", 1)
                .notIn("age", Arrays.asList(11L, 111L, 1111L))
                .notIn("name", "小明", "小红")
                .getFilters();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("id", Collections.singletonMap("$nin", Collections.singletonList(1)));
        expectFilters.put("age", Collections.singletonMap("$nin", Arrays.asList(11L, 111L, 1111L)));
        expectFilters.put("name", Collections.singletonMap("$nin", Arrays.asList("小明", "小红")));

        Assertions.assertThat(expectFilters).isEqualTo(filters);
    }

    @Test
    void testLess() {
        Map<String, Object> filters = Query.newBuilder()
                .notIn("id", 1)
                .lt("age", 18)
                .lte("score", 99)
                .getFilters();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("id", Collections.singletonMap("$nin", Collections.singletonList(1)));
        expectFilters.put("age", Collections.singletonMap("$lt", 18));
        expectFilters.put("score", Collections.singletonMap("$lte", 99));

        Assertions.assertThat(expectFilters).isEqualTo(filters);
    }

    @Test
    void testGreater() {
        Map<String, Object> filters = Query.newBuilder()
                .notIn("id", 1)
                .gt("age", 18)
                .gte("score", 99)
                .getFilters();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("id", Collections.singletonMap("$nin", Collections.singletonList(1)));
        expectFilters.put("age", Collections.singletonMap("$gt", 18));
        expectFilters.put("score", Collections.singletonMap("$gte", 99));

        Assertions.assertThat(expectFilters).isEqualTo(filters);
    }

    @Test
    void testBetween() {
        Map<String, Object> filters = Query.newBuilder()
                .notIn("id", 1)
                .between("age", 12, 18)
                .betweenExcludeLeft("score", 90, 100)
                .betweenExcludeRight("grade", 1, 6)
                .betweenExcludeAll("height", 1.2, 1.8)
                .getFilters();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("id", Collections.singletonMap("$nin", Collections.singletonList(1)));
        expectFilters.put("age", from(entry("$gte", 12), entry("$lte", 18)));
        expectFilters.put("score", from(entry("$gt", 90), entry("$lte", 100)));
        expectFilters.put("grade", from(entry("$gte", 1), entry("$lt", 6)));
        expectFilters.put("height", from(entry("$gt", 1.2), entry("$lt", 1.8)));

        Assertions.assertThat(expectFilters).isEqualTo(filters);
    }

    @Test
    void testSum() {
        Map<String, Object> groupFields = Query.newBuilder()
                .summary("score").sum()
                .summary("age").sum("sumAge")
                .getGroupFields();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("score", from(entry("$sum", "score")));
        expectFilters.put("sumAge", from(entry("$sum", "age")));

        Assertions.assertThat(expectFilters).isEqualTo(groupFields);
    }

    @Test
    void testMin() {
        Map<String, Object> groupFields = Query.newBuilder()
                .summary("score").min()
                .summary("age").min("minAge")
                .getGroupFields();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("score", from(entry("$min", "score")));
        expectFilters.put("minAge", from(entry("$min", "age")));

        Assertions.assertThat(expectFilters).isEqualTo(groupFields);
    }

    @Test
    void testMax() {
        Map<String, Object> groupFields = Query.newBuilder()
                .summary("score").max()
                .summary("age").max("maxAge")
                .getGroupFields();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("score", from(entry("$max", "score")));
        expectFilters.put("maxAge", from(entry("$max", "age")));

        Assertions.assertThat(expectFilters).isEqualTo(groupFields);
    }

    @Test
    void testAvg() {
        Map<String, Object> groupFields = Query.newBuilder()
                .summary("score").avg()
                .summary("age").avg("avgAge")
                .getGroupFields();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("score", from(entry("$avg", "score")));
        expectFilters.put("avgAge", from(entry("$avg", "age")));

        Assertions.assertThat(expectFilters).isEqualTo(groupFields);
    }

    @Test
    void testCount() {
        Map<String, Object> groupFields = Query.newBuilder()
                .summary("score").count()
                .summary("age").count("countAge")
                .getGroupFields();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("score", from(entry("$count", "score")));
        expectFilters.put("countAge", from(entry("$count", "age")));

        Assertions.assertThat(expectFilters).isEqualTo(groupFields);
    }

    @Test
    void testFirst() {
        Map<String, Object> groupFields = Query.newBuilder()
                .summary("score").first()
                .summary("age").first("firstAge")
                .getGroupFields();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("score", from(entry("$first", "score")));
        expectFilters.put("firstAge", from(entry("$first", "age")));

        Assertions.assertThat(expectFilters).isEqualTo(groupFields);
    }
    
    @Test
    void testLast() {
        Map<String, Object> groupFields = Query.newBuilder()
                .summary("score").last()
                .summary("age").last("lastAge")
                .getGroupFields();

        Map<String, Object> expectFilters = new HashMap<>();
        expectFilters.put("score", from(entry("$last", "score")));
        expectFilters.put("lastAge", from(entry("$last", "age")));

        Assertions.assertThat(expectFilters).isEqualTo(groupFields);
    }
}
