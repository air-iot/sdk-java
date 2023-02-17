package cn.airiot.sdk.client.service;


import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * 平台客户端基础接口. 仅用于标识该客户端属于平台接口
 */
public interface PlatformClient {

    /**
     * Map 类型
     */
    Class<Map<String, Object>> MAP_TYPE = (Class<Map<String, Object>>) TypeToken.getParameterized(Map.class, String.class, Object.class).getRawType();

}
