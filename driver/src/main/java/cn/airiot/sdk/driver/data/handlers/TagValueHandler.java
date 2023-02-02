package cn.airiot.sdk.driver.data.handlers;

import cn.airiot.sdk.driver.data.DataHandler;
import cn.airiot.sdk.driver.data.model.Tag;
import cn.airiot.sdk.driver.data.model.TagValue;

/**
 * 数据点值映射处理.
 * <br>
 * 将当前数据点的值按在 {@code minRawValue} 和 {@code maxRawValue} 间位置(百分比, 即: value / (maxRawValue - minRawValue)) 映射到
 * {@code minValue} 和 {@code maxValue} 间相同百分比的值
 * <br>
 * <p>
 * 计算规则:
 * (当前值 - minRawValue) / (maxRawValue - minRawValue) * (maxValue - minValue) + minValue
 *
 * <pre>
 *     假设场景, 某温度计可测量温度范围为 -50.00 到 50.00, 精确到小数点后2位, 并且使用整数值表示温度(即接收到的数据为实际温度 * 100), 例如: 实际温度为 24.52, 接收到的值为 2452.
 *     使用映射功能将数值还原实际温度.
 *
 *     表示接收到的温度值测量范围: 温度值 * 100
 *     minRawValue: -5000
 *     maxRawValue: 5000
 *
 *     实际温度值测量范围
 *     minValue: -50
 *     maxValue: 50
 *
 *     1. 当接收到的温度值为 -5000 时, 映射后的值为 -50
 *     2. 当接收到的温度值为 0 时, 映射后的值为 0
 *     3. 当接收到的温度值为 5000 时, 映射后的值为 50
 *     4. 当接收到的温度值为 2358 时, 映射后的值为 23.58
 *     5. 当接收到的温度值为 -2358 时, 映射后的值为 -23.58
 * </pre>
 */
public class TagValueHandler implements DataHandler {

    @Override
    public boolean supports(String deviceId, Tag tag, Object value) {
        if (tag == null || tag.getTagValue() == null) {
            return false;
        }

        if (!DataHandler.super.supports(deviceId, tag, value)) {
            return false;
        }

        TagValue mapping = tag.getTagValue();
        return mapping.getMinValue() != null && mapping.getMaxValue() != null
                && mapping.getMinRaw() != null && mapping.getMaxRaw() != null;
    }

    @Override
    public Object handle(String deviceId, Tag tag, Object value) {
        double val = ((Number) value).doubleValue();
        TagValue tagValue = tag.getTagValue();

        double minRawValue = tagValue.getMinRaw();
        double maxRawValue = tagValue.getMaxRaw();
        double minValue = tagValue.getMinValue();
        double maxValue = tagValue.getMaxValue();

        val = Math.max(val, minRawValue);
        val = Math.min(val, maxRawValue);

        if (minRawValue == maxRawValue) {
            return val;
        }

        return (val - minRawValue) / (maxRawValue - minRawValue) * (maxValue - minValue) + minValue;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
