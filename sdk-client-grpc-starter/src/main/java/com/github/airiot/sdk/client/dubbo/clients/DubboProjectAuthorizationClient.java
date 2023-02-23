package com.github.airiot.sdk.client.dubbo.clients;

import com.github.airiot.sdk.client.dto.Token;
import com.github.airiot.sdk.client.dubbo.grpc.api.Response;
import com.github.airiot.sdk.client.dubbo.grpc.api.TokenRequest;
import com.github.airiot.sdk.client.dubbo.grpc.core.DubboAppServiceGrpc;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.exception.AuthorizationException;
import com.github.airiot.sdk.client.service.AuthorizationClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 项目级授权客户端
 */
public class DubboProjectAuthorizationClient implements AuthorizationClient {

    private final AtomicReference<Token> holder = new AtomicReference<>();
    private final DubboAppServiceGrpc.IAppService appService;
    private final String appKey;
    private final String appSecret;

    public DubboProjectAuthorizationClient(DubboAppServiceGrpc.IAppService appService, String appKey, String appSecret) {
        this.appService = appService;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    @Override
    public synchronized Token getToken() {
        Token token = this.holder.get();
        if (token != null && !token.isExpired(Duration.ofSeconds(60))) {
            return token;
        }

        Response response = this.appService.getToken(TokenRequest.newBuilder()
                .setAk(this.appKey)
                .setSk(this.appSecret)
                .build());
        token = DubboClientUtils.deserialize(Token.class, response)
                .unwrap(() -> new AuthorizationException(response.getCode(), response.getInfo(), response.getDetail()));

        this.holder.set(token);
        return token;
    }
}
