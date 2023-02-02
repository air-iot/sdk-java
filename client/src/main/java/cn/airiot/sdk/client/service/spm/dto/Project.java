package cn.airiot.sdk.client.service.spm.dto;

import java.util.Date;
import java.util.Map;


/**
 * 项目信息
 */
public class Project {
    /**
     * 项目唯一标识
     */
    private String id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 用户基础信息
     */
    private User user;

    /**
     * 行业
     */
    private String industry;

    /**
     * 授权
     */
    private Map<String, Object> grant;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 模型创建时间
     */
    private Date createTime;

    /**
     *
     */
    private String bgColor;

    /**
     * 项目类型
     */
    private String projectType;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 已用授权
     */
    private LicenseContent license;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Map<String, Object> getGrant() {
        return grant;
    }

    public void setGrant(Map<String, Object> grant) {
        this.grant = grant;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public LicenseContent getLicense() {
        return license;
    }

    public void setLicense(LicenseContent license) {
        this.license = license;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", user='" + user + '\'' +
                ", industry='" + industry + '\'' +
                ", grant=" + grant +
                ", remarks='" + remarks + '\'' +
                ", createTime=" + createTime +
                ", bgColor='" + bgColor + '\'' +
                ", projectType='" + projectType + '\'' +
                ", status=" + status +
                ", license=" + license +
                '}';
    }
}

