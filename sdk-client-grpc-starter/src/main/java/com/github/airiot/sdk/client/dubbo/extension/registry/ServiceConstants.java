package com.github.airiot.sdk.client.dubbo.extension.registry;


/**
 * 常量
 */
public interface ServiceConstants {

    /**
     * 服务实例ID
     * <br>
     * 元数据存储在 {@link org.apache.dubbo.common.URL} 的 attribute 中
     */
    String SERVICE_INSTANCE_ID = "instanceId";

    /**
     * 元数据 Key
     * <br>
     * 元数据存储在 {@link org.apache.dubbo.common.URL} 的 attribute 中
     */
    String METADATA_KEY = "metadata";

    /**
     * 服务实例版本号
     * <br>
     * 元数据存储在 {@link org.apache.dubbo.common.URL} 的 attribute 中
     */
    String VERSION_KEY = "version";
}
