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

import java.util.Date;


/**
 * 项目信息
 */
public class Project {
    /**
     * 项目唯一标识
     * <br>
     * 创建项目时无须填写
     */
    private String id;

    /**
     * 项目名称, 必填
     */
    private String name;

    /**
     * 用户基础信息
     * <br>
     * 创建项目时无须填写
     */
    private User user;

    /**
     * 行业
     */
    private String industry;

    /**
     * 授权
     */
    private LicenseContent grant;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 模型创建时间
     * <br>
     * 创建项目时无须填写
     */
    private Date createTime;

    /**
     * 用于设定项目管理卡片展示时，卡片的背景色
     */
    private String bgColor;

    /**
     * 项目类型
     */
    private String projectType;

    /**
     * 项目启用状态
     * <br>
     * true: 启用, false: 禁用
     */
    private Boolean status;

    /**
     * 已用授权
     * <br>
     * 创建项目时无须填写, 该字段信息由平台自动维护
     */
    private LicenseContent license;

    public Project() {

    }

    public Project(String name, LicenseContent grant) {
        this.name = name;
        this.grant = grant;
    }

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

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public LicenseContent getGrant() {
        return grant;
    }

    public void setGrant(LicenseContent grant) {
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

