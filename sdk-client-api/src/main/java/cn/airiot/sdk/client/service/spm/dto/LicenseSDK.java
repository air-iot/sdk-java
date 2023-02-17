package cn.airiot.sdk.client.service.spm.dto;


/**
 * 二次开发引擎
 */
public class LicenseSDK {

    /**
     * app数量
     */
    private Integer appCount;

    public Integer getAppCount() {
        return appCount;
    }

    public void setAppCount(Integer appCount) {
        this.appCount = appCount;
    }

    @Override
    public String toString() {
        return "LicenseSDK{" +
                "appCount=" + appCount +
                '}';
    }
}
