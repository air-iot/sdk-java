package cn.airiot.sdk.client.dubbo.clients.spm;

import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dto.Token;
import cn.airiot.sdk.client.dubbo.grpc.api.TokenRequest;
import cn.airiot.sdk.client.dubbo.grpc.spm.DubboUserServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.service.spm.SpmUserClient;
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

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.userService.getToken(
                TokenRequest.newBuilder()
                        .setSk(appKey)
                        .setSk(appSecret).build());

        return DubboClientUtils.deserialize(Token.class, response);
    }
}
