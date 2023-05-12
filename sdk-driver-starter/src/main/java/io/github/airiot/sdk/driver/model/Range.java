package io.github.airiot.sdk.driver.model;


import java.util.List;

/**
 * 数据点-有效范围配置.
 * 如果采集到的数据不在 {@link Range#getMinValue()} 和 {@link Range#getMaxValue()} 范围内时, 会对数据做相关处理然后将处理后的结果数据发送到平台
 * <br>
 * 注: 只有采集到数据的类型为数值类型时才有效. 并且该处理过程已经集成在 sdk 中, 默认无须关注
 *
 * <br>
 * 示例配置如下:
 * <pre>
 *  {
 * 	    "minValue": 0,
 * 	    "maxValue": 0,
 * 	    "conditions": [{
 * 		    "mode": "number",
 * 		    "condition": "range",
 * 		    "minValue": 0,
 * 		    "maxValue": 100,
 * 		    "value": 0,
 * 		    "defaultCondition": true
 *       }],
 * 	    "active": "boundary",
 * 	    "fixedValue": 10,
 * 	    "invalidAction": "save"
 * }
 * </pre>
 */
public class Range {

    public static class Condition {
        /**
         * 有效值处理模式
         * <br>
         * 取值: number(数值), rate(变化率), delta(差值)
         * <br>
         * number: 比较当前采集到的数据是否在设定的有效范围内
         * <br>
         * rate: 比较当前采集到的数据与上一次采集到的数据的变化率是否在设定的有效范围内
         * 变化率 = (当前值 - 上一次处理后的结果值) / 上一次处理后的结果值 * 100
         * <br>
         * delta: 比较当前采集到的数据与上一次采集到的数据的差值是否在设定的有效范围内
         * 差值 = 当前值 - 上一次处理后的结果值
         */
        private String mode;
        /**
         * 有效值判断条件类型.
         * <br>
         * 取值: range(范围值), greater(大于), less (小于)
         * <br>
         * range: 当前采集到的数据必须在设定的有效范围内. 即: minValue <= value <= maxValue
         * <br>
         * greater: 当前采集到的数据必须大于设定的有效范围. 即: value > maxValue
         * <br>
         * less: 当前采集到的数据必须小于设定的有效范围. 即: value < minValue
         */
        private String condition;
        /**
         * 有效范围最小值
         * <br>
         * 只有当 {@code condition} 为 range 时有效
         */
        private Double minValue;
        /**
         * 有效范围最大值
         * <br>
         * 只有当 {@code condition} 为 range 时有效
         */
        private Double maxValue;
        /**
         * 大于或小于的有效值
         * <br>
         * 只有当 {@code condition} 为 greater 或 less 时有效
         */
        private Double value;
        /**
         * 默认处理条件. 即当前值与所有条件都不匹配时, 并且 {@code active} 为 boundary 时, 使用该条件的配置进行处理
         */
        private Boolean defaultCondition;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public Double getMinValue() {
            return minValue;
        }

        public void setMinValue(Double minValue) {
            this.minValue = minValue;
        }

        public Double getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(Double maxValue) {
            this.maxValue = maxValue;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public Boolean getDefaultCondition() {
            return defaultCondition;
        }

        public void setDefaultCondition(Boolean defaultCondition) {
            this.defaultCondition = defaultCondition;
        }

        public Condition() {
        }

        public Condition(String mode, String condition, Double minValue, Double maxValue, Double value, Boolean defaultCondition) {
            this.mode = mode;
            this.condition = condition;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.value = value;
            this.defaultCondition = defaultCondition;
        }

        public Condition(String mode, String condition, Double minValue, Double maxValue, Boolean defaultCondition) {
            this.mode = mode;
            this.condition = condition;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.value = null;
            this.defaultCondition = defaultCondition;
        }
        
        public Condition(String mode, String condition, Double value, Boolean defaultCondition) {
            this.mode = mode;
            this.condition = condition;
            this.minValue = null;
            this.maxValue = null;
            this.value = value;
            this.defaultCondition = defaultCondition;
        }

        @Override
        public String toString() {
            return "Condition{" +
                    "mode='" + mode + '\'' +
                    ", condition='" + condition + '\'' +
                    ", minValue=" + minValue +
                    ", maxValue=" + maxValue +
                    ", value=" + value +
                    ", defaultCondition=" + defaultCondition +
                    '}';
        }
    }

    /**
     * 新版本的有效范围配置, 用于支持多种有效范围配置
     */
    private List<Condition> conditions;
    /**
     * 最小值
     */
    private Double minValue;
    /**
     * 最大值
     */
    private Double maxValue;
    /**
     * 固定值, 仅当 {@link #active} 为 {@code fixed} 时有效
     */
    private Double fixedValue;
    /**
     * 当数据点采集到的数值超出设定范围时执行的动作. 可取值如下:
     * <br><br>
     * fixed: 固定值, 即返回 {@link #fixedValue} 定义的值
     * <br><br><br>
     * boundary: 边界值.
     * <br>
     * 之前版本处理: 如果采集到的数据小于 {@link #minValue} 时返回 {@link #minValue}, 如果大于 {@link #maxValue} 时返回 {@link #maxValue}<br>
     * 新版本处理: 根据 {@link #conditions} 中第一条规则的 {@code mode} 和 {@code condition} 进行处理<br>
     * 如果第一条规则的 {@code mode} 和 {@code condition} 进行处理
     * <br><br><br>
     * latest: 取最新有效值, 即返回上一次处理后的结果值
     * <br><br><br>
     * discard: 丢弃, 即不上报该数据
     */
    private String active;
    /**
     * 无效值处理动作. 目前可取值: save(保存)
     * <br>
     * 当勾选时, 会将无效值保存到平台, 字段名为 {@link Tag#getId()} + "__invalid"
     */
    private String invalidAction;

    public Range() {
    }

    public Range(Double minValue, Double maxValue, Double fixedValue, String active) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.active = active;
        this.fixedValue = fixedValue;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Double getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(Double fixedValue) {
        this.fixedValue = fixedValue;
    }

    public String getInvalidAction() {
        return invalidAction;
    }

    public void setInvalidAction(String invalidAction) {
        this.invalidAction = invalidAction;
    }

    @Override
    public String toString() {
        return "Range{" +
                "conditions=" + conditions +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", fixedValue=" + fixedValue +
                ", active='" + active + '\'' +
                ", invalidAction='" + invalidAction + '\'' +
                '}';
    }
}
