package io.github.airiot.sdk.client.http.clients;

import io.github.airiot.sdk.client.context.RequestContext;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.service.AuthorizationClient;
import io.github.airiot.sdk.client.service.Constants;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.util.StringUtils;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClientRequest;

import java.net.SocketAddress;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class WebClientUtils {

    /**
     * 身份认证处理
     */
    public static BiConsumer<? super HttpClientRequest, ? super SocketAddress> httpAuthentication(AuthorizationClient authorizationClient) {
        return (HttpClientRequest request, SocketAddress address) -> {
            HttpHeaders headers = request.requestHeaders();
            // 如果已经有了 Authorization 头，说明已经认证过了，不需要再次认证
            if (StringUtils.hasText(headers.get(Constants.HEADER_AUTHORIZATION))) {
                return;
            }
            Token token = authorizationClient.getToken();
            headers.add(Constants.HEADER_AUTHORIZATION, token.getToken());
        };
    }

    public static BiConsumer<? super HttpClientRequest, ? super SocketAddress> httpAuthentication(Supplier<AuthorizationClient> authorizationClient) {
        return (HttpClientRequest request, SocketAddress address) -> {
            HttpHeaders headers = request.requestHeaders();
            // 如果已经有了 Authorization 头，说明已经认证过了，不需要再次认证
            if (StringUtils.hasText(headers.get(Constants.HEADER_AUTHORIZATION))) {
                return;
            }
            Token token = authorizationClient.get().getToken();
            headers.add(Constants.HEADER_AUTHORIZATION, token.getToken());
        };
    }

    public static BiConsumer<? super HttpClientRequest, ? super Connection> proxyProjectId() {
        return (request, conn) -> {
            String projectId = RequestContext.getProjectId();
            request.requestHeaders().set(Constants.HEADER_PROJECT, projectId);
        };
    }
}
