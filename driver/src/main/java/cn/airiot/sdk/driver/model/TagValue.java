package cn.airiot.sdk.driver.model;

public class TagValue {
    private Float minValue;
    private Float maxValue;
    private Float minRaw;
    private Float maxRaw;

    public TagValue() {
    }

    public TagValue(Float minValue, Float maxValue, Float minRaw, Float maxRaw) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minRaw = minRaw;
        this.maxRaw = maxRaw;
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

    public Float getMinRaw() {
        return minRaw;
    }

    public void setMinRaw(Float minRaw) {
        this.minRaw = minRaw;
    }

    public Float getMaxRaw() {
        return maxRaw;
    }

    public void setMaxRaw(Float maxRaw) {
        this.maxRaw = maxRaw;
    }

    @Override
    public String toString() {
        return "TagValue{" +
                "minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", minRaw=" + minRaw +
                ", maxRaw=" + maxRaw +
                '}';
    }
}


