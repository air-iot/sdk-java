package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.model.Tag;
import org.springframework.core.Ordered;


/**
 * 数据处理器, 对采集到的数据进行处理
 * <br>
 * 系统中存在多个 {@link DataHandler} 时, 会按 {@link DataHandler#getOrder()} 从小到大进行排序, 并依次执行.
 * 即 {@code order} 值越小优先级越高
 */
public interface DataHandler extends Ordered {

    /**
     * 判断当前处理器是否支持目标 {@link Tag} 或 {@code value}.
     * <br>
     * 默认处理所有值不为 {@code null} 的数据
     *
     * @param deviceId 设备ID
     * @param tag      数据点信息
     * @param value    采集到的数据
     * @return 如果返回值为 {@code true} 则调用 {@link #handle(String, Tag, Object)} 对采集到的数据进行处理, 否则跳过当前处理器
     */
    default boolean supports(String deviceId, Tag tag, Object value) {
        return value != null;
    }

    /**
     * 对采集到的数据进行处理, 并返回处理后的结果
     *
     * @param deviceId 设备ID
     * @param tag      数据点信息
     * @param value    采集到的数据
     * @return 处理后的结果数据. 如果返回结果为 {@code null} 则表示丢弃该数据点的数据
     */
    Object handle(String deviceId, Tag tag, Object value);

}
