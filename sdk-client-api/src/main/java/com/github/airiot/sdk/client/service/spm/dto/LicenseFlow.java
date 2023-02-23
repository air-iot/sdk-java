package com.github.airiot.sdk.client.service.spm.dto;


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
