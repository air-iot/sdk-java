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

package com.github.airiot.sdk.client.service.spm.dto;

import java.util.List;

/**
 * 授权信息
 */
public class LicenseContent {

    /**
     * 机器码
     */
    private List<String> macAddr;
    /**
     * 授权类型: license 或 license-spm
     */
    private String licenseType;
    /**
     * 授权开始时间
     * <br>
     * 创建授权时必填
     */
    private String startTime;
    /**
     * 有效期
     * <br>
     * 创建授权时必填
     */
    private Integer validityPeriod;
    /**
     * 用户数
     * <br>
     * 创建授权时必填
     */
    private Integer userCount;
    /**
     * 剩余有效期
     */
    private Integer remainingTime;
    /**
     * 授权版本
     */
    private String version;
    /**
     *
     */
    private Boolean status;
    /**
     * 数据采集与控制引擎授权信息
     */
    private LicenseDataGathering dataGathering;
    /**
     * 可视化组态引擎授权信息
     */
    private LicenseVisual visual;
    /**
     * 数据分析引擎授权信息
     */
    private LicenseDataAnalysis dataAnalysis;
    /**
     * 业务流引擎授权信息
     */
    private LicenseFlow flow;
    /**
     * 二次开发引擎授权信息
     */
    private LicenseSDK sdk;

    public LicenseContent() {
    }

    /**
     * 创建基本授权所需的信息
     */
    public LicenseContent(String startTime, Integer validityPeriod, Integer userCount) {
        this.startTime = startTime;
        this.validityPeriod = validityPeriod;
        this.userCount = userCount;
    }

    public List<String> getMacAddr() {
        return macAddr;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Integer getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getRemainingTime() {
        return remainingTime;
    }

    public String getVersion() {
        return version;
    }
    
    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public LicenseDataGathering getDataGathering() {
        return dataGathering;
    }

    public void setDataGathering(LicenseDataGathering dataGathering) {
        this.dataGathering = dataGathering;
    }

    public LicenseVisual getVisual() {
        return visual;
    }

    public void setVisual(LicenseVisual visual) {
        this.visual = visual;
    }

    public LicenseDataAnalysis getDataAnalysis() {
        return dataAnalysis;
    }

    public void setDataAnalysis(LicenseDataAnalysis dataAnalysis) {
        this.dataAnalysis = dataAnalysis;
    }

    public LicenseFlow getFlow() {
        return flow;
    }

    public void setFlow(LicenseFlow flow) {
        this.flow = flow;
    }

    public LicenseSDK getSdk() {
        return sdk;
    }

    public void setSdk(LicenseSDK sdk) {
        this.sdk = sdk;
    }

    @Override
    public String toString() {
        return "LicenseContent{" +
                "macAddr=" + macAddr +
                ", licenseType='" + licenseType + '\'' +
                ", startTime='" + startTime + '\'' +
                ", validityPeriod=" + validityPeriod +
                ", userCount=" + userCount +
                ", remainingTime=" + remainingTime +
                ", version='" + version + '\'' +
                ", status=" + status +
                ", dataGathering=" + dataGathering +
                ", visual=" + visual +
                ", dataAnalysis=" + dataAnalysis +
                ", flow=" + flow +
                ", sdk=" + sdk +
                '}';
    }
}
