package cn.airiot.sdk.driver.data.handlers;

import cn.airiot.sdk.driver.data.DataHandler;
import cn.airiot.sdk.driver.data.model.Range;
import cn.airiot.sdk.driver.data.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangeValueHandler implements DataHandler {

    private final Logger log = LoggerFactory.getLogger(RangeValueHandler.class);

    @Override
    public boolean supports(String deviceId, Tag tag, Object value) {
        boolean matched;
        String tagId = "";
        Range range = null;
        if (tag != null) {
            tagId = tag.getId();
            range = tag.getRange();
        }

        if (!DataHandler.super.supports(deviceId, tag, value) || tag == null) {
            matched = false;
        } else {
            matched = value instanceof Number
                    && range != null && range.getActive() != null
                    && range.getMinValue() != null && range.getMaxValue() != null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Range 数据处理器: Node[{}], Tag[{}], Range[{}], Value[{}], 匹配结果[{}]",
                    deviceId, tagId, range, value, matched);
        }
        return matched;
    }

    @Override
    public Object handle(String deviceId, Tag tag, Object value) {
        double val = ((Number) value).doubleValue();
        Range range = tag.getRange();

        double minValue = range.getMinValue().doubleValue();
        double maxValue = range.getMaxValue().doubleValue();

        String tagId = tag.getId();

        log.debug("Range 数据处理器: Node[{}], Tag[{}], Value[{}], 有效范围[{}]", deviceId, tagId, val, range);

        if (val >= minValue && val <= maxValue) {
            log.debug("Range 数据处理器: Node[{}], Tag[{}], Value[{}], 在有效范围内[{} - {}], 无须处理",
                    deviceId, tagId, val, minValue, maxValue);
            return val;
        }

        log.debug("Range 数据处理器: Node[{}], Tag[{}], Value[{}], 超出有效范围内[{} - {}]",
                deviceId, tagId, val, minValue, maxValue);

        switch (range.getActive()) {
            case "fixed":
                if (range.getFixedValue() == null) {
                    throw new IllegalArgumentException("Tag[" + tagId + "]有效范围动作为固定值, 但未提供有效的固定值");
                }

                double fixedValue = range.getFixedValue().doubleValue();

                log.debug("Range 数据处理器: Node[{}], Tag[{}], Value[{}], 转换为固定值[{}]",
                        deviceId, tagId, val, fixedValue);

                return fixedValue;
            case "boundary":
                double finalValue = minValue > val ? minValue : maxValue;

                log.debug("Range 数据处理器: Node[{}], Tag[{}], Value[{}], 转换为边界值[{}]",
                        deviceId, tagId, val, finalValue);

                return finalValue;
            case "discard":
                log.debug("Range 数据处理器: Node[{}], Tag[{}], Value[{}], 丢弃", deviceId, tagId, val);
                return null;
            default:
                throw new IllegalArgumentException("未定义的有效范围动作: " + range.getActive());
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
