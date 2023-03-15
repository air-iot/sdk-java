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


/**
 * 可视化组态引擎
 */
public class LicenseVisual {

    /**
     * 画面数量
     */
    private Integer dashboardCount;
    /**
     * 视频路数
     */
    private Integer videoCount;
    /**
     * 录像组件
     */
    private Boolean video;
    /**
     * GIS地图
     */
    private Boolean gis;
    /**
     * 三维组件
     */
    private Boolean threeD;

    public Integer getDashboardCount() {
        return dashboardCount;
    }

    public void setDashboardCount(Integer dashboardCount) {
        this.dashboardCount = dashboardCount;
    }

    public Integer getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(Integer videoCount) {
        this.videoCount = videoCount;
    }

    public Boolean getVideo() {
        return video;
    }

    public void setVideo(Boolean video) {
        this.video = video;
    }

    public Boolean getGis() {
        return gis;
    }

    public void setGis(Boolean gis) {
        this.gis = gis;
    }

    public Boolean getThreeD() {
        return threeD;
    }

    public void setThreeD(Boolean threeD) {
        this.threeD = threeD;
    }

    @Override
    public String toString() {
        return "LicenseVisual{" +
                "dashboardCount=" + dashboardCount +
                ", videoCount=" + videoCount +
                ", video=" + video +
                ", gis=" + gis +
                ", threeD=" + threeD +
                '}';
    }
}
