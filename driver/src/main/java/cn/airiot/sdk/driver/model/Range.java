package cn.airiot.sdk.driver.model;


/**
 * 数据点 {@code 有效范围} 配置信息.
 * 如果采集到的数据不在 {@link Range#getMinValue()} 和 {@link Range#getMaxValue()} 范围内时, 会对数据做相关处理然后将处理后的结果数据发送到平台
 * <br>
 * 注: 只有采集到数据的类型为数值类型时才有效. 并且该处理过程已经集成在 sdk 中, 默认无须关注
 */
public class Range {
    /**
     * 最小值
     */
    private Float minValue;
    /**
     * 最大值
     */
    private Float maxValue;
    /**
     * 动作. 可取值如下:
     * <br>
     * fixed: 固定值, 即返回 {@link #fixedValue} 定义的值
     * <br>
     * boundary: 边界值, 如果采集到的数据小于 {@link #minValue} 时返回 {@link #minValue}, 如果大于 {@link #maxValue} 时返回 {@link #maxValue}
     * <br>
     * discard: 丢弃, 即不上报该数据
     */
    private String active;
    /**
     * 固定值, 仅当 {@link #active} 为 {@code fixed} 时有效
     */
    private Float fixedValue;

    public Range() {
    }

    public Range(Float minValue, Float maxValue, String active, Float fixedValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.active = active;
        this.fixedValue = fixedValue;
    }

    public Float getMinValue() {
        return minValue;
    }

    public void setMinValue(Float minValue) {
        this.minValue = minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Float maxValue) {
        this.maxValue = maxValue;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Float getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(Float fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public String toString() {
        return "Range{" +
                "minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", active='" + active + '\'' +
                ", fixedValue=" + fixedValue +
                '}';
    }
}
