package com.github.airiot.sdk.client.service.spm.dto;


/**
 * 数据分析引擎
 */
public class LicenseDataAnalysis {

    /**
     * 报表数量
     */
    private Integer reportCount;
    /**
     * 生成链接数量
     */
    private Integer dataAnalysisLinkCount;

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public Integer getDataAnalysisLinkCount() {
        return dataAnalysisLinkCount;
    }

    public void setDataAnalysisLinkCount(Integer dataAnalysisLinkCount) {
        this.dataAnalysisLinkCount = dataAnalysisLinkCount;
    }

    @Override
    public String toString() {
        return "LicenseDataAnalysis{" +
                "reportCount=" + reportCount +
                ", dataAnalysisLinkCount=" + dataAnalysisLinkCount +
                '}';
    }
}
