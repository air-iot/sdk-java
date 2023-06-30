package io.github.airiot.sdk.driver.data.handlers;

import io.github.airiot.sdk.driver.data.DataHandler;
import io.github.airiot.sdk.driver.model.Range;
import io.github.airiot.sdk.driver.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * 数据点-新版本有效范围
 * <br>
 * 根据 {@link Range#getConditions()} 的配置进行处理
 *
 * @see Range
 */
public class RangeValueHandlerV2 implements DataHandler {

    private final Logger logger = LoggerFactory.getLogger(RangeValueHandlerV2.class);

    private final Set<String> actions = new HashSet<>(Arrays.asList("fixed", "boundary", "discard", "latest"));

    /**
     * 浮点数精度
     */
    private final int precision;
    private final TagValueCache tagValueCache;

    public RangeValueHandlerV2(TagValueCache tagValueCache) {
        this(tagValueCache, 10);
    }

    public RangeValueHandlerV2(TagValueCache tagValueCache, int precision) {
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
            logger.debug("数据点数据处理器: 有效范围处理, 不生效. table = {}, device = {}, tag = {}, range = {}, value = {}",
                    tableId, deviceId, tagId, range, value);
            return false;
        }

        if (CollectionUtils.isEmpty(range.getConditions())) {
            return false;
        }

        if (range.getMinValue() != null || range.getMaxValue() != null) {
            logger.debug("数据点数据处理器: 有效范围处理, 不生效. table = {}, device = {}, tag = {}, range = {}, value = {}",
                    tableId, deviceId, tagId, range, value);
            return false;
        }

        if (!actions.contains(range.getActive())) {
            logger.warn("数据点数据处理器: 有效范围处理, 无效的动作 {}, table = {}, device = {}, tag = {}, range = {}, value = {}",
                    range.getActive(), tableId, deviceId, tagId, range, value);
            return false;
        }

//        if ("fixed".equals(range.getActive()) && range.getFixedValue() == null) {
//            logger.warn("数据点数据处理器: 有效范围处理, 未提供有效的固定值, device = {}, tag = {}, range = {}",
//                    deviceId, tagId, range);
//            return false;
//        }

        logger.debug("数据点数据处理器: 有效范围处理, 生效. table = {}, device = {}, tag = {}, range = {}, value = {}",
                tableId, deviceId, tagId, range, value);

        return true;
    }

    @Override
    public Map<String, Object> handle(String tableId, String deviceId, Tag tag, Object value) {
        double dValue = ((Number) value).doubleValue();
        if (!Double.isFinite(dValue)) {
            logger.warn("数据点数据处理器: 有效范围处理, 值为 {}, 丢弃. table = {}, device = {}, tag = {}",
                    value, tableId, deviceId, tag.getId());
            return Collections.emptyMap();
        }

        String tagId = tag.getId();
        Range range = tag.getRange();

        BigDecimal val = BigDecimal.valueOf(((Number) value).doubleValue());
        TagValueCache.CacheValue latestValue = this.tagValueCache.get(tableId, deviceId, tagId);
        BigDecimal latestVal = latestValue == null ? null : BigDecimal.valueOf(((Number) latestValue.getValue()).doubleValue());

        String active = range.getActive().toLowerCase();
        boolean isBoundary = "boundary".equals(active);

        List<Result> results = new ArrayList<>(range.getConditions().size());
        for (Range.Condition condition : range.getConditions()) {
            Result result = this.handleCondition(condition, val, latestVal, isBoundary);
            if (result.isMatched()) {
                return Collections.singletonMap(tagId, result.getValue().doubleValue());
            } else if (!result.isSkipped()) {
                results.add(result);
            }
        }

        // 如果所有条件都跳过, 直接返回当前值
        if (results.isEmpty()) {
            logger.debug("数据点数据处理器: 有效范围处理, 所有条件均跳过, 返回原始值. table = {}, device = {}, tag = {}, range = {}, value = {}",
                    tableId, deviceId, tagId, range, value);
            return Collections.singletonMap(tagId, value);
        }

        Map<String, Object> tagValues = new HashMap<>(3);
        if ("save".equals(tag.getRange().getInvalidAction())) {
            tagValues.put(tag.getInvalidTagId(), value);
        }

        BigDecimal finalValue = null;

        // 如果所有条件都不匹配, 执行相应动作
        switch (active) {
            case "fixed":
                if (range.getFixedValue() == null) {
                    logger.warn("数据点数据处理器: 有效范围处理, 有效范围动作为固定值, 但未提供有效的固定值. table = {}, device = {}, tag = {}, range = {}, value = {}",
                            tableId, deviceId, tagId, range, value);
                    return tagValues;
                }

                double fixedValue = range.getFixedValue();

                logger.debug("数据点数据处理器: 有效范围处理, table = {}, device = {}, tag = {}, range = {}, value = {}, 转换为固定值 {}",
                        tableId, deviceId, tagId, range, value, fixedValue);

                finalValue = BigDecimal.valueOf(fixedValue);
                tagValues.put(tagId, finalValue.doubleValue());
                return tagValues;
            case "boundary":
                Optional<Result> defaultResult = results.stream()
                        .filter(r -> !r.isSkipped())
                        .filter(Result::isDefault)
                        .findFirst();
                if (!defaultResult.isPresent()) {
                    logger.warn("数据点数据处理器: 有效范围处理, 所有条件均不匹配, 但未设置. 值为 {}, 丢弃. table = {}, device = {}, tag = {}",
                            value, tableId, deviceId, tag.getId());
                    return tagValues;
                }

                finalValue = defaultResult.get().getValue();
                logger.debug("数据点数据处理器: 有效范围处理, table = {}, device = {}, tag = {}, range = {}, value = {}, 转换为边界值 {}",
                        tableId, deviceId, tagId, range, value, finalValue);
                tagValues.put(tagId, finalValue.doubleValue());
                return tagValues;
            case "latest":
                if (latestVal == null) {
                    logger.debug("数据点数据处理器: 有效范围处理, 未找到最新有效值, 取当前值. table = {}, device = {}, tag = {}, range = {}, value = {}",
                            tableId, deviceId, tagId, range, value);
                    tagValues.put(tagId, value);
                } else {
                    logger.debug("数据点数据处理器: 有效范围处理, 使用最新有效值. table = {}, device = {}, tag = {}, range = {}, value = {}",
                            tableId, deviceId, tagId, range, latestVal);
                    tagValues.put(tagId, latestVal.doubleValue());
                }
                return tagValues;
            case "discard":
                logger.debug("数据点数据处理器: 有效范围处理, table = {}, device = {}, tag = {}, range = {}, value = {}, 丢弃数据",
                        tableId, deviceId, tagId, range, value);
                return tagValues;
            default:
                throw new IllegalArgumentException("无数据点的有效范围处理失败, 设备 '" + deviceId + "' 数据点 '" + tagId + "', 未定义的有效范围处理动作: " + range.getActive());
        }
    }

    private Result handleCondition(Range.Condition condition, BigDecimal value, BigDecimal latestValue, boolean isBoundary) {
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
        }

        switch (conditionType) {
            case "range":
                BigDecimal minValue = BigDecimal.valueOf(condition.getMinValue());
                BigDecimal maxValue = BigDecimal.valueOf(condition.getMaxValue());
                // 如果在有效值范围内, 则匹配
                if (calcValue.compareTo(minValue) >= 0 && calcValue.compareTo(maxValue) <= 0) {
                    return Result.matched(condition, value);
                }

                // 如果不是边界模式, 则不计算有效值
                if (!isBoundary) {
                    return Result.nonMatched(condition, null);
                }

                switch (mode) {
                    case "number":
                        return Result.nonMatched(condition, value.compareTo(minValue) < 0 ? minValue : maxValue);
                    case "rate":
                        // 如果变化率 < minValue
                        if (calcValue.compareTo(minValue) < 0) {
                            // ((x - latestValue) / latestValue) * 100 = minValue
                            // (minValue / 100) * latestValue + latestValue
                            return Result.nonMatched(condition, minValue.divide(BigDecimal.valueOf(100)).multiply(latestValue).add(latestValue));
                        } else {
                            // ((x - latestValue) / latestValue) * 100 = maxValue
                            // (maxValue / 100) * latestValue + latestValue
                            return Result.nonMatched(condition, maxValue.divide(BigDecimal.valueOf(100)).multiply(latestValue).add(latestValue));
                        }
                    case "delta":
                        // 如果差值 < minValue
                        if (calcValue.compareTo(minValue) < 0) {
                            // x - latestValue = minValue
                            // x = latestValue + minValue

                            // 5 < delta < 10
                            // x = 8, latestValue = 5
                            // x - latestValue = 3 < 5
                            // x - 5 = 5
                            // x = 10

                            return Result.nonMatched(condition, latestValue.add(minValue));
                        } else {
                            // x - latestValue = minValue
                            // x = latestValue + minValue

                            // 5 < delta < 10
                            // x = 8, latestValue = 5
                            // x - latestValue = 3 < 5
                            // x - 5 = 10
                            // x = 15

                            return Result.nonMatched(condition, latestValue.add(maxValue));
                        }
                }
            case "greater":
                BigDecimal greaterValue = BigDecimal.valueOf(condition.getValue());
                if (calcValue.compareTo(greaterValue) > 0) {
                    return Result.matched(condition, value);
                }

                // 如果不是边界模式, 则不计算有效值
                if (!isBoundary) {
                    return Result.nonMatched(condition, null);
                }

                switch (mode) {
                    case "number":
                        return Result.nonMatched(condition, greaterValue);
                    case "rate":
                        // ((x - latestValue) / latestValue) * 100 = greaterValue
                        // (greaterValue / 100) * latestValue + latestValue
                        return Result.nonMatched(condition, greaterValue.divide(BigDecimal.valueOf(100)).multiply(latestValue).add(latestValue));
                    case "delta":
                        // x - latestValue = greaterValue
                        // x = latestValue + greaterValue

                        // delta > 5
                        // x = 8, latestValue = 5
                        // x - latestValue = 3 < 5
                        // x - 5 = 10
                        // x = 15

                        return Result.nonMatched(condition, latestValue.add(greaterValue));
                }
            case "less":
                BigDecimal lessValue = BigDecimal.valueOf(condition.getValue());
                if (calcValue.compareTo(lessValue) < 0) {
                    return Result.matched(condition, value);
                }

                // 如果不是边界模式, 则不计算有效值
                if (!isBoundary) {
                    return Result.nonMatched(condition, null);
                }

                switch (mode) {
                    case "number":
                        return Result.nonMatched(condition, lessValue);
                    case "rate":
                        // ((x - latestValue) / latestValue) * 100 = greaterValue
                        // (greaterValue / 100) * latestValue + latestValue
                        return Result.nonMatched(condition, lessValue.divide(BigDecimal.valueOf(100)).multiply(latestValue).add(latestValue));
                    case "delta":
                        // x - latestValue = greaterValue
                        // x = latestValue + greaterValue

                        // delta > 5
                        // x = 8, latestValue = 5
                        // x - latestValue = 3 < 5
                        // x - 5 = 10
                        // x = 15

                        return Result.nonMatched(condition, latestValue.add(lessValue));
                }
        }

        throw new IllegalStateException("未定义的 mode: " + mode + ", condition: " + conditionType);
    }

    @Override
    public int getOrder() {
        return 201;
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
