package cn.airiot.sdk.client.service.spm.dto;


/**
 * 数据采集与控制引擎
 */
public class LicenseDataGathering {

    /**
     * 点数
     */
    private Integer point;
    /**
     * 计算点数
     */
    private Integer calcPoint;

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Integer getCalcPoint() {
        return calcPoint;
    }

    public void setCalcPoint(Integer calcPoint) {
        this.calcPoint = calcPoint;
    }

    @Override
    public String toString() {
        return "LicenseDataGathering{" +
                "point=" + point +
                ", calcPoint=" + calcPoint +
                '}';
    }
}
