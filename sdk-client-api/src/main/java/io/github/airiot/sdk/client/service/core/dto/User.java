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

package io.github.airiot.sdk.client.service.core.dto;

import com.google.gson.annotations.JsonAdapter;
import io.github.airiot.sdk.client.gson.StringToMapFieldAdapterFactory;

import java.util.Date;

/**
 * 用户信息
 */
public class User {
    /**
     * 用户唯一标识
     */
    private String id;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 判断用户是否是超级管理员的标志位
     */
    private Boolean isSuper;

    /**
     * 判断用户是否是分享用户
     */
    private Boolean isShare;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户微信名称
     */
    private String openName;

    /**
     * 电话信息
     */
    private String phone;

    /**
     * 行业配置
     */
    private String industry;

    /**
     * 用户创建时间
     */
    private Date createTime;

    /**
     * 额外属性
     */
    private String extra;

    /**
     * 仅管理个人资产
     */
    private Boolean onlyNode;

    /**
     * 有效时长(秒)
     */
    private Long expireUse;

    /**
     * 旧有效时长(秒)
     */
    private Long expires;

    /**
     * 画面id
     */
    private String dashboard;

    /**
     * 不查看子部门报警数据
     */
    private Boolean noChildDept;

    /**
     * 禁用
     */
    private Boolean disabled;

    /**
     * adminAccess
     */
    private Boolean adminAccess;

    /**
     * 页配置
     */
    private String pageSetting;

    /**
     * 主页菜单配置
     */
    private String mainmenu;

    /**
     * 分享画面类型
     */
    private String shareUserType;

    /**
     * 分享画面超时时间
     */
    private Date timeoutTimeUse;

    /**
     * 报警分组配置
     */
    private String warningFilter;

    /**
     * 类型
     */
    private String type;

    /**
     * 不操作预期超时时间
     */
    private Date noOpTimeouttime;

    /**
     * 不操作预期超时时间(中台)
     */
    private Date noOpTimeouttimeAdmin;

    /**
     * 强制修改密码预期超时时间
     */
    private Date pwdTimeouttime;

    /**
     * 强制修改密码时长配置
     */
    @JsonAdapter(StringToMapFieldAdapterFactory.class)
    private String pwdExpireConfig;

    /**
     * 管理表
     */
    private String tableSetting;

    /**
     * 管理表记录
     */
    private String tableDataSetting;

    /**
     * 备注
     */
    private String remark;

    /**
     * 冻结预期超时时间
     */
    private Date errorLoginTimeouttime;

    /**
     * 冻结预期超时时间(中台)
     */
    private Date errorLoginTimeouttimeAdmin;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSuper() {
        return isSuper;
    }

    public void setSuper(Boolean aSuper) {
        isSuper = aSuper;
    }

    public Boolean getShare() {
        return isShare;
    }

    public void setShare(Boolean share) {
        isShare = share;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpenName() {
        return openName;
    }

    public void setOpenName(String openName) {
        this.openName = openName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public Boolean getOnlyNode() {
        return onlyNode;
    }

    public void setOnlyNode(Boolean onlyNode) {
        this.onlyNode = onlyNode;
    }

    public Long getExpireUse() {
        return expireUse;
    }

    public void setExpireUse(Long expireUse) {
        this.expireUse = expireUse;
    }

    public Long getExpires() {
        return expires;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }

    public String getDashboard() {
        return dashboard;
    }

    public void setDashboard(String dashboard) {
        this.dashboard = dashboard;
    }

    public Boolean getNoChildDept() {
        return noChildDept;
    }

    public void setNoChildDept(Boolean noChildDept) {
        this.noChildDept = noChildDept;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean getAdminAccess() {
        return adminAccess;
    }

    public void setAdminAccess(Boolean adminAccess) {
        this.adminAccess = adminAccess;
    }

    public String getPageSetting() {
        return pageSetting;
    }

    public void setPageSetting(String pageSetting) {
        this.pageSetting = pageSetting;
    }

    public String getMainmenu() {
        return mainmenu;
    }

    public void setMainmenu(String mainmenu) {
        this.mainmenu = mainmenu;
    }

    public String getShareUserType() {
        return shareUserType;
    }

    public void setShareUserType(String shareUserType) {
        this.shareUserType = shareUserType;
    }

    public Date getTimeoutTimeUse() {
        return timeoutTimeUse;
    }

    public void setTimeoutTimeUse(Date timeoutTimeUse) {
        this.timeoutTimeUse = timeoutTimeUse;
    }

    public String getWarningFilter() {
        return warningFilter;
    }

    public void setWarningFilter(String warningFilter) {
        this.warningFilter = warningFilter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getNoOpTimeouttime() {
        return noOpTimeouttime;
    }

    public void setNoOpTimeouttime(Date noOpTimeouttime) {
        this.noOpTimeouttime = noOpTimeouttime;
    }

    public Date getNoOpTimeouttimeAdmin() {
        return noOpTimeouttimeAdmin;
    }

    public void setNoOpTimeouttimeAdmin(Date noOpTimeouttimeAdmin) {
        this.noOpTimeouttimeAdmin = noOpTimeouttimeAdmin;
    }

    public Date getPwdTimeouttime() {
        return pwdTimeouttime;
    }

    public void setPwdTimeouttime(Date pwdTimeouttime) {
        this.pwdTimeouttime = pwdTimeouttime;
    }

    public String getPwdExpireConfig() {
        return pwdExpireConfig;
    }

    public void setPwdExpireConfig(String pwdExpireConfig) {
        this.pwdExpireConfig = pwdExpireConfig;
    }

    public String getTableSetting() {
        return tableSetting;
    }

    public void setTableSetting(String tableSetting) {
        this.tableSetting = tableSetting;
    }

    public String getTableDataSetting() {
        return tableDataSetting;
    }

    public void setTableDataSetting(String tableDataSetting) {
        this.tableDataSetting = tableDataSetting;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getErrorLoginTimeouttime() {
        return errorLoginTimeouttime;
    }

    public void setErrorLoginTimeouttime(Date errorLoginTimeouttime) {
        this.errorLoginTimeouttime = errorLoginTimeouttime;
    }

    public Date getErrorLoginTimeouttimeAdmin() {
        return errorLoginTimeouttimeAdmin;
    }

    public void setErrorLoginTimeouttimeAdmin(Date errorLoginTimeouttimeAdmin) {
        this.errorLoginTimeouttimeAdmin = errorLoginTimeouttimeAdmin;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", isSuper=" + isSuper +
                ", isShare=" + isShare +
                ", email='" + email + '\'' +
                ", openName='" + openName + '\'' +
                ", phone='" + phone + '\'' +
                ", industry='" + industry + '\'' +
                ", createTime=" + createTime +
                ", extra='" + extra + '\'' +
                ", onlyNode=" + onlyNode +
                ", expireUse=" + expireUse +
                ", expires=" + expires +
                ", dashboard='" + dashboard + '\'' +
                ", noChildDept=" + noChildDept +
                ", disabled=" + disabled +
                ", adminAccess=" + adminAccess +
                ", pageSetting='" + pageSetting + '\'' +
                ", mainmenu='" + mainmenu + '\'' +
                ", shareUserType='" + shareUserType + '\'' +
                ", timeoutTimeUse=" + timeoutTimeUse +
                ", warningFilter='" + warningFilter + '\'' +
                ", type='" + type + '\'' +
                ", noOpTimeouttime=" + noOpTimeouttime +
                ", noOpTimeouttimeAdmin=" + noOpTimeouttimeAdmin +
                ", pwdTimeouttime=" + pwdTimeouttime +
                ", pwdExpireConfig='" + pwdExpireConfig + '\'' +
                ", tableSetting='" + tableSetting + '\'' +
                ", tableDataSetting='" + tableDataSetting + '\'' +
                ", remark='" + remark + '\'' +
                ", errorLoginTimeouttime=" + errorLoginTimeouttime +
                ", errorLoginTimeouttimeAdmin=" + errorLoginTimeouttimeAdmin +
                '}';
    }
}

