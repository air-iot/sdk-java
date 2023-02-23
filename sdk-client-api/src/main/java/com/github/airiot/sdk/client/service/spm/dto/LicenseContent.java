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
     */
    private String startTime;
    /**
     * 有效期
     */
    private Integer validityPeriod;
    /**
     * 用户数
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
     * 数据采集与控制引擎
     */
    private LicenseDataGathering dataGathering;
    /**
     * 可视化组态引擎
     */
    private LicenseVisual visual;
    /**
     * 数据分析引擎
     */
    private LicenseDataAnalysis dataAnalysis;
    /**
     * 业务流引擎
     */
    private LicenseFlow flow;
    /**
     * 二次开发引擎
     */
    private LicenseSDK sdk;

    public List<String> getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(List<String> macAddr) {
        this.macAddr = macAddr;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
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

    public void setRemainingTime(Integer remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
