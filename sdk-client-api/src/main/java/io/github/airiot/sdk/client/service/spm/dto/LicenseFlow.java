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

package io.github.airiot.sdk.client.service.spm.dto;


/**
 * 流程引擎
 */
public class LicenseFlow {

    /**
     * 流程数量
     */
    private Integer flowCount;
    /**
     * 流程执行数量
     */
    private Integer flowExecCount;
    /**
     * 工作表数量
     */
    private Integer tableCount;

    public Integer getFlowCount() {
        return flowCount;
    }

    public void setFlowCount(Integer flowCount) {
        this.flowCount = flowCount;
    }

    public Integer getFlowExecCount() {
        return flowExecCount;
    }

    public void setFlowExecCount(Integer flowExecCount) {
        this.flowExecCount = flowExecCount;
    }

    public Integer getTableCount() {
        return tableCount;
    }

    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }

    @Override
    public String toString() {
        return "LicenseFlow{" +
                "flowCount=" + flowCount +
                ", flowExecCount=" + flowExecCount +
                ", tableCount=" + tableCount +
                '}';
    }
}
