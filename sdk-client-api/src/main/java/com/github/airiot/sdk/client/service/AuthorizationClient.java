package com.github.airiot.sdk.client.service;

import com.github.airiot.sdk.client.annotation.DisableAuth;
import com.github.airiot.sdk.client.dto.Token;
import com.github.airiot.sdk.client.exception.AuthorizationException;

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
