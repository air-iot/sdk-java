package cn.airiot.sdk.client.service.core;


import cn.airiot.sdk.client.annotation.DisableAuth;
import cn.airiot.sdk.client.dto.Token;
import cn.airiot.sdk.client.service.PlatformClient;

/**
 * 扩展应用客户端
 */
public interface AppClient extends PlatformClient {

    /**
     * 使用 {@code appKey} 和 {@code appSecret} 获取项目级 token
     *
     * @param appKey    扩展应用标识
     * @param appSecret 扩展应用密钥
     * @return token
     */
    @DisableAuth
    Token getToken(String appKey, String appSecret);

}
