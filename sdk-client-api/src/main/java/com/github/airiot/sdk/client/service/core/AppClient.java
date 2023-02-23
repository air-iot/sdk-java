package com.github.airiot.sdk.client.service.core;


import com.github.airiot.sdk.client.annotation.DisableAuth;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dto.Token;
import com.github.airiot.sdk.client.service.PlatformClient;

import javax.annotation.Nonnull;

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
    Response<Token> getToken(@Nonnull String appKey, @Nonnull String appSecret);

}
