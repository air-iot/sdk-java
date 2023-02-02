package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.model.Point;
import cn.airiot.sdk.driver.data.model.Tag;

/**
 * 采集数据处理链
 */
public interface DataHandlerChain {

    /**
     * 依次执行所有的处理器对采集到的数据进行处理, 并返回最终处理结果
     *
     * @param nodeId 资产ID
     * @param tag    数据点信息
     * @param value  采集到的数据
     * @return 处理后的结果数据. 如果返回结果为 {@code null} 则表示丢弃该数据点的数据
     */
    Object handle(String nodeId, Tag tag, Object value);

    /**
     * 依次执行所有的处理器对一个资产下所有采集到的数据进行处理, 并返回最终处理结果
     *
     * @param point 资产采集到的数据
     * @return 处理后的结果数据
     */
    Point handle(Point point);

}
