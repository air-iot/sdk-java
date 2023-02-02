package cn.airiot.sdk.client.dubbo.clients;

import cn.airiot.sdk.client.annotation.NonProject;
import cn.airiot.sdk.client.dto.Token;
import cn.airiot.sdk.client.dubbo.grpc.api.Response;
import cn.airiot.sdk.client.dubbo.grpc.api.TokenRequest;
import cn.airiot.sdk.client.dubbo.grpc.spm.DubboUserServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.exception.AuthorizationException;
import cn.airiot.sdk.client.service.AuthorizationClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 租户级授权客户端
 */
@NonProject
public class DubboTenantAuthorizationClient implements AuthorizationClient {

    private final AtomicReference<Token> holder = new AtomicReference<>();
    private final DubboUserServiceGrpc.IUserService userService;
    private final String appKey;
    private final String appSecret;

    public DubboTenantAuthorizationClient(DubboUserServiceGrpc.IUserService userService, String appKey, String appSecret) {
        this.userService = userService;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    @Override
    public synchronized Token getToken() {
        Token token = this.holder.get();
        if (token != null && !token.isExpired(Duration.ofSeconds(60))) {
            return token;
        }

        Response response = this.userService.getToken(TokenRequest.newBuilder()
                .setAk(this.appKey)
                .setSk(this.appSecret)
                .build());

        token = DubboClientUtils.deserialize(Token.class, response)
                .unwrap(() -> new AuthorizationException(response.getCode(), response.getInfo(), response.getDetail()));
        this.holder.set(token);
        return token;
    }
}
