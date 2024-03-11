package io.github.airiot.sdk.driver.data.handlers;

import io.github.airiot.sdk.driver.data.DataHandler;
import io.github.airiot.sdk.driver.model.Range;
import io.github.airiot.sdk.driver.model.Tag;
import io.github.airiot.sdk.logger.LoggerFactory;
import io.github.airiot.sdk.logger.driver.DriverModules;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * 数据点-无效范围
 * <br>
 * 根据 {@link Range#getConditions()} 的配置进行处理
 *
 * @see Range
 */
public class InvalidRangeValueHandler implements DataHandler {

    private final Logger logger = LoggerFactory.withContext().module(DriverModules.WRITE_POINTS).getDynamicLogger(InvalidRangeValueHandler.class);

    private final Set<String> actions = new HashSet<>(Arrays.asList("fixed", "boundary", "discard", "latest"));

    /**
     * 浮点数精度
     */
    private final int precision;
    private final TagValueCache tagValueCache;

    public InvalidRangeValueHandler(TagValueCache tagValueCache) {
        this(tagValueCache, 10);
    }

    public InvalidRangeValueHandler(TagValueCache tagValueCache, int precision) {
        this.tagValueCache = tagValueCache;
        this.precision = precision;
    }

    @Override
    public boolean supports(String tableId, String deviceId, Tag tag, Object value) {
        String tagId = "";
        Range range = null;
        if (tag != null) {
            tagId = tag.getId();
            range = tag.getRange();
        }

        if (!DataHandler.super.supports(tableId, deviceId, tag, value) || tag == null || range == null || !(value instanceof Number)) {
            logger.debug("数据点数据处理器: 无效范围处理, 不生效. table = {}, device = {}, tag = {}, range = {}, value = {}",
                    tableId, deviceId, tagId, range, value);
            return false;
        }

        if (CollectionUtils.isEmpty(range.getConditions())) {
            return false;
        }

        if (!"invalid".equalsIgnoreCase(range.getMethod())) {
            logger.debug("数据点数据处理器: 无效范围处理, 不生效. table = {}, device = {}, tag = {}, range = {}, value = {}",
                    tableId, deviceId, tagId, range, value);
            return false;
        }

        if (range.getMinValue() != null || range.getMaxValue() != null) {
            logger.debug("数据点数据处理器: 无效范围处理, 不生效. table = {}, device = {}, tag = {}, range = {}, value = {}",
                    tableId, deviceId, tagId, range, value);
            return false;
        }

        if (!actions.contains(range.getActive())) {
            logger.warn("数据点数据处理器: 无效范围处理, 无效的动作 {}, table = {}, device = {}, tag = {}, range = {}, value = {}",
                    range.getActive(), tableId, deviceId, tagId, range, value);
            return false;
        }

        logger.debug("数据点数据处理器: 无效范围处理, 生效. table = {}, device = {}, tag = {}, range = {}, value = {}",
                tableId, deviceId, tagId, range, value);

        return true;
    }

    @Override
    public Map<String, Object> handle(String tableId, String deviceId, Tag tag, Object value) {
        double dValue = ((Number) value).doubleValue();
        if (Double.isNaN(dValue) || !Double.isFinite(dValue)) {
            logger.warn("数据点数据处理器: 无效范围处理, 值为 {}, 丢弃. table = {}, device = {}, tag = {}",
                    value, tableId, deviceId, tag.getId());
            return Collections.emptyMap();
        }

        String tagId = tag.getId();
        Range range = tag.getRange();

        BigDecimal val = BigDecimal.valueOf(((Number) value).doubleValue());
        TagValueCache.CacheValue latestValue = this.tagValueCache.get(tableId, deviceId, tagId);
        BigDecimal latestVal = latestValue == null ? null : BigDecimal.valueOf(((Number) latestValue.getValue()).doubleValue());

        String active = range.getActive().toLowerCase();

        Result matchedResult = null;
        for (Range.Condition condition : range.getConditions()) {
            Result result = this.handleCondition(condition, val, latestVal);
            if (result.isSkipped()) {
                continue;
            }
            if (result.isMatched()) {
                matchedResult = result;
                break;
            }
        }

        // 未匹配到任何条件, 说明该值有效
        if (matchedResult == null) {
            logger.debug("数据点数据处理器: 无效范围处理, 未匹配到任何条件, 返回原始值. table = {}, device = {}, tag = {}, range = {}, value = {}",
                    tableId, deviceId, tagId, range, value);
            return Collections.singletonMap(tagId, value);
        }

        Map<String, Object> tagValues = new HashMap<>(4);
        // 无效类型
        tagValues.put(tag.getInvalidType(), matchedResult.getCondition().getInvalidType());
        if ("save".equals(tag.getRange().getInvalidAction())) {
            // 无效值
            tagValues.put(tag.getInvalidTagId(), value);
        }

        // 如果所有条件都不匹配, 执行相应动作
        switch (active) {
            case "fixed":
                if (range.getFixedValue() == null) {
                    logger.warn("数据点数据处理器: 无效范围处理, 有效范围动作为固定值, 但未提供有效的固定值. table = {}, device = {}, tag = {}, range = {}, value = {}",
                            tableId, deviceId, tagId, range, value);
                    return tagValues;
                }

                double fixedValue = range.getFixedValue();

                logger.debug("数据点数据处理器: 无效范围处理, table = {}, device = {}, tag = {}, range = {}, value = {}, 转换为固定值 {}",
                        tableId, deviceId, tagId, range, value, fixedValue);

                tagValues.put(tagId, BigDecimal.valueOf(fixedValue).doubleValue());
                return tagValues;
            case "latest":
                if (latestVal == null) {
                    logger.debug("数据点数据处理器: 无效范围处理, 未找到最新有效值, 取当前值. table = {}, device = {}, tag = {}, range = {}, value = {}",
                            tableId, deviceId, tagId, range, value);
                    tagValues.put(tagId, value);
                } else {
                    logger.debug("数据点数据处理器: 无效范围处理, 使用最新有效值. table = {}, device = {}, tag = {}, range = {}, value = {}",
                            tableId, deviceId, tagId, range, latestVal);
                    tagValues.put(tagId, latestVal.doubleValue());
                }
                return tagValues;
            case "discard":
                logger.debug("数据点数据处理器: 无效范围处理, table = {}, device = {}, tag = {}, range = {}, value = {}, 丢弃数据",
                        tableId, deviceId, tagId, range, value);
                return tagValues;
            default:
                throw new IllegalArgumentException("无数据点的无效范围处理失败, 设备 '" + deviceId + "' 数据点 '" + tagId + "', 未定义的无效范围处理动作: " + range.getActive());
        }
    }

    private Result handleCondition(Range.Condition condition, BigDecimal value, BigDecimal latestValue) {
        String mode = condition.getMode();
        String conditionType = condition.getCondition();

        // 如果没有最新有效值或为 0, 并且不是 number 模式, 则跳过处理
        // 因为 rate 和 delta 模式, 需要最新有效值
        if ((latestValue == null || latestValue.compareTo(BigDecimal.ZERO) == 0) && "rate".equals(mode)) {
            return Result.skipped(condition);
        } else if (latestValue == null && "delta".equals(mode)) {
            return Result.skipped(condition);
        }

        BigDecimal calcValue = null;
        switch (mode) {
            case "number":
                calcValue = value;
                break;
            case "rate":
                calcValue = value.subtract(latestValue)
                        .divide(latestValue, this.precision, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                break;
            case "delta":
                calcValue = value.subtract(latestValue);
                break;
            default:
                throw new IllegalStateException("未定义的 mode: " + mode + ", condition: " + conditionType);
        }

        switch (conditionType) {
            case "range":
                BigDecimal minValue = BigDecimal.valueOf(condition.getMinValue());
                BigDecimal maxValue = BigDecimal.valueOf(condition.getMaxValue());
                // 如果在有效值范围内, 则匹配
                if (calcValue.compareTo(minValue) >= 0 && calcValue.compareTo(maxValue) <= 0) {
                    return Result.matched(condition, value);
                }
                break;
            case "greater":
                BigDecimal greaterValue = BigDecimal.valueOf(condition.getValue());
                if (calcValue.compareTo(greaterValue) > 0) {
                    return Result.matched(condition, value);
                }
                break;
            case "less":
                BigDecimal lessValue = BigDecimal.valueOf(condition.getValue());
                if (calcValue.compareTo(lessValue) < 0) {
                    return Result.matched(condition, value);
                }
                break;
            default:
                throw new IllegalStateException("无效的条件类型: " + conditionType);
        }

        return Result.nonMatched(condition, value);
    }

    @Override
    public int getOrder() {
        return 301;
    }

    static class Result {
        /**
         * 匹配条件信息
         */
        private final Range.Condition condition;
        /**
         * 是否与当前条件匹配
         * <br>
         * 如果匹配, 则为 {@code true}, 否则为 {@code false}
         */
        private final boolean matched;
        /**
         * 是否跳过了当前匹配条件
         */
        private final boolean skipped;
        /**
         * 处理后的结果值
         */
        private final BigDecimal value;

        public boolean isDefault() {
            return this.condition.getDefaultCondition() != null && this.condition.getDefaultCondition();
        }

        public boolean isSkipped() {
            return skipped;
        }

        public Range.Condition getCondition() {
            return condition;
        }

        public boolean isMatched() {
            return matched;
        }

        public BigDecimal getValue() {
            return value;
        }

        public Result(Range.Condition condition, boolean matched, boolean skipped, BigDecimal value) {
            this.condition = condition;
            this.matched = matched;
            this.value = value;
            this.skipped = skipped;
        }


        static Result matched(Range.Condition condition, BigDecimal value) {
            return new Result(condition, true, false, value);
        }

        static Result nonMatched(Range.Condition condition, BigDecimal value) {
            return new Result(condition, false, false, value);
        }

        static Result skipped(Range.Condition condition) {
            return new Result(condition, false, true, null);
        }
    }
}
