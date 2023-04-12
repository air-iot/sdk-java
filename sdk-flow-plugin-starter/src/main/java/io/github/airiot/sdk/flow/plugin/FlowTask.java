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

package io.github.airiot.sdk.flow.plugin;


/**
 * 流程任务信息
 *
 * @param <T> 流程任务的配置信息
 */
public class FlowTask<T> {

    /**
     * 当前流程所属项目ID
     */
    private final String projectId;
    /**
     * 当前流程ID
     */
    private final String flowId;
    /**
     * 流程实例ID
     */
    private final String job;
    /**
     * 当前节点ID
     */
    private final String elementId;
    /**
     * 节点实例ID
     */
    private final String elementJob;
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

    public String getJob() {
        return job;
    }

    public String getElementId() {
        return elementId;
    }

    public String getElementJob() {
        return elementJob;
    }

    public T getConfig() {
        return config;
    }

    public FlowTask(String projectId, String flowId, String job, String elementId, String elementJob, T config) {
        this.projectId = projectId;
        this.flowId = flowId;
        this.job = job;
        this.elementId = elementId;
        this.elementJob = elementJob;
        this.config = config;
    }

    @Override
    public String toString() {
        return "Job{" +
                "projectId='" + projectId + '\'' +
                ", flowId='" + flowId + '\'' +
                ", job='" + job + '\'' +
                ", elementId='" + elementId + '\'' +
                ", elementJob='" + elementJob + '\'' +
                ", config=" + config +
                '}';
    }
}
