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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产点最新数据查询
 * <br>
 * 支持查询指定的数据点, 也可以查询设备内的所有数据点的最新数据.
 *
 * <pre>
 *
 *     LatestDataQuery.create()
 *          .specific("demo", "demo001", "tag1", "tag3", "tag5")    // 查询设备 demo001 的 tag1、tag3 和 tag5 三个数据点的最新数据
 *          .allTags("demo", "demo002");                            // 查询设备 demo002 所有数据点的最新数据
 *
 *
 * </pre>
 */
public class LatestDataQuery {

    private final List<Specification> specifications = new ArrayList<>();

    public static LatestDataQuery create() {
        return new LatestDataQuery();
    }

    public List<Specification> getSpecifications() {
        return specifications;
    }
    
    /**
     * 查询设备中指定的数据点最新数据
     *
     * @param tableId 工作表标识
     * @param nodeId  资产编号
     * @param tags    数据点标识列表
     */
    public LatestDataQuery specific(String tableId, String nodeId, String... tags) {
        return this.specific(tableId, nodeId, Arrays.asList(tags));
    }

    /**
     * 查询设备中指定的数据点最新数据
     *
     * @param tableId 工作表标识
     * @param nodeId  资产编号
     * @param tags    数据点标识列表
     */
    public LatestDataQuery specific(String tableId, String nodeId, Collection<String> tags) {
        if (!StringUtils.hasText(tableId)) {
            throw new IllegalArgumentException("the tableId cannot be empty");
        }

        if (!StringUtils.hasText(nodeId)) {
            throw new IllegalArgumentException("the nodeId cannot be empty");
        }

        List<String> tagIds = tags.stream().filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (tagIds.isEmpty()) {
            throw new IllegalArgumentException("the tag list is invalid");
        }

        for (String tagId : tagIds) {
            this.specifications.add(new Specification(nodeId, tableId, tagId));
        }

        return this;
    }

    /**
     * 查询指定资产的全部数据点的最新数据
     *
     * @param tableId 工作表标识
     * @param nodeId  资产编号
     */
    public LatestDataQuery allTags(String tableId, String nodeId) {
        if (!StringUtils.hasText(tableId)) {
            throw new IllegalArgumentException("the tableId cannot be empty");
        }

        if (!StringUtils.hasText(nodeId)) {
            throw new IllegalArgumentException("the nodeId cannot be empty");
        }

        this.specifications.add(new Specification(nodeId, tableId, true));
        return this;
    }

    public static class Specification {
        /**
         * 资产编号
         */
        private final String id;
        /**
         * 资产所在工作表标识
         */
        private final String tableId;
        /**
         * 是否查询设备下全部数据点
         */
        private final Boolean allTag;
        /**
         * 要查询的数据点标识列表
         */
        private final String tagId;

        public String getId() {
            return id;
        }

        public String getTableId() {
            return tableId;
        }

        public Boolean getAllTag() {
            return allTag;
        }

        public String getTagId() {
            return tagId;
        }

        public Specification(String id, String tableId, boolean allTag) {
            this.id = id;
            this.tableId = tableId;
            this.allTag = allTag;
            this.tagId = null;
        }

        public Specification(String id, String tableId, String tagId) {
            this.id = id;
            this.tableId = tableId;
            this.allTag = null;
            this.tagId = tagId;
        }
    }
}
