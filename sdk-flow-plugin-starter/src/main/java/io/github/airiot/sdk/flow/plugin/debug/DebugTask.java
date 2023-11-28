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

package io.github.airiot.sdk.flow.plugin.debug;


/**
 * 流程插件调试信息
 */
public class DebugTask<T> {
    /**
     * 流程所属项目ID
     */
    private final String projectId;
    /**
     * 流程ID
     */
    private final String flowId;
    /**
     * 当前节点ID
     */
    private final String elementId;
    /**
     * 节点配置信息
     */
    private final T config;

    public String getProjectId() {
        return projectId;
    }

    public String getFlowId() {
        return flowId;
    }

    public String getElementId() {
        return elementId;
    }

    public T getConfig() {
        return config;
    }

    public DebugTask(String projectId, String flowId, String elementId, T config) {
        this.projectId = projectId;
        this.flowId = flowId;
        this.elementId = elementId;
        this.config = config;
    }

    @Override
    public String toString() {
        return "DebugTask{" +
                "projectId='" + projectId + '\'' +
                ", flowId='" + flowId + '\'' +
                ", elementId='" + elementId + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
