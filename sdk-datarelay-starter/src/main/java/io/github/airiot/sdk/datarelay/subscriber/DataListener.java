package io.github.airiot.sdk.datarelay.subscriber;


@FunctionalInterface
public interface DataListener {

    /**
     * 订阅器发收到设备数据时的回调函数
     *
     * @param device 最新的设备数据
     */
    void onMessage(DeviceData device);

}
