package com.github.airiot.sdk.client.dubbo.clients.spm;

import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dto.Token;
import com.github.airiot.sdk.client.dubbo.grpc.api.TokenRequest;
import com.github.airiot.sdk.client.dubbo.grpc.spm.DubboUserServiceGrpc;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.service.spm.SpmUserClient;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;


public class DubboSpmUserClient implements SpmUserClient {

    private final DubboUserServiceGrpc.IUserService userService;

    public DubboSpmUserClient(DubboUserServiceGrpc.IUserService userService) {
        this.userService = userService;
    }

    @Override
    public Response<Token> getToken(@Nonnull String appKey, @Nonnull String appSecret) {
        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(appSecret)) {
            throw new IllegalArgumentException("'appKey' and 'appSecret' cannot be empty");
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.userService.getToken(
                TokenRequest.newBuilder()
                        .setSk(appKey)
                        .setSk(appSecret).build());

        return DubboClientUtils.deserialize(Token.class, response);
    }
}
