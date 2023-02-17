package cn.airiot.sdk.client.service;

import cn.airiot.sdk.client.annotation.DisableAuth;
import cn.airiot.sdk.client.dto.Token;
import cn.airiot.sdk.client.exception.AuthorizationException;

/**
 * 认证客户端
 */
@DisableAuth
public interface AuthorizationClient extends PlatformClient {

    /**
     * 获取 token
     *
     * @return token
     */
    Token getToken() throws AuthorizationException;

}
