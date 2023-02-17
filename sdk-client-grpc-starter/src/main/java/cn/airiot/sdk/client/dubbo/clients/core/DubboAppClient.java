package cn.airiot.sdk.client.dubbo.clients.core;

import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dto.Token;
import cn.airiot.sdk.client.dubbo.grpc.api.TokenRequest;
import cn.airiot.sdk.client.dubbo.grpc.core.DubboAppServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.service.core.AppClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;


public class DubboAppClient implements AppClient {

    private final Logger logger = LoggerFactory.getLogger(DubboAppClient.class);

    private final DubboAppServiceGrpc.IAppService appService;

    public DubboAppClient(DubboAppServiceGrpc.IAppService appService) {
        this.appService = appService;
    }

    @Override
    public Response<Token> getToken(@Nonnull String appKey, @Nonnull String appSecret) {
        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(appSecret)) {
            throw new IllegalArgumentException("'appKey' and 'appSecret' cannot be empty");
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.appService.getToken(
                TokenRequest.newBuilder()
                        .setSk(appKey)
                        .setSk(appSecret).build());

        return DubboClientUtils.deserialize(Token.class, response);
    }
}
