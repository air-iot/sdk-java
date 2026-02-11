/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.airiot.sdk.client.http.clients.common;


import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.exception.RequestFailedException;
import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.http.clients.WebClientUtils;
import io.github.airiot.sdk.client.service.AuthorizationClient;
import io.github.airiot.sdk.client.service.Constants;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.BiConsumer;

/**
 * 通用的 HTTP 客户端.
 */
public class HttpCommonClient {

    private final ConnectionProvider provider;
    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration callTimeout;
    private final Duration writeTimeout;
    private final BiConsumer<? super HttpClientRequest, ? super SocketAddress> authenticator;

    public HttpCommonClient(String baseUrl, AuthorizationClient authorizationClient,
                            Duration connectTimeout, Duration callTimeout, Duration writeTimeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.connectTimeout = connectTimeout;
        this.callTimeout = callTimeout;
        this.writeTimeout = writeTimeout;

        this.authenticator = WebClientUtils.httpAuthentication(authorizationClient);
        this.provider = ConnectionProvider.builder("airiot")
                .maxConnections(20)
                .maxIdleTime(Duration.ofSeconds(60))
                .maxLifeTime(Duration.ofSeconds(300))
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();
    }

    HttpClient createClient(Duration connectTimeout) {
        return HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis())
                .httpAuthentication((req, address) -> true, this.authenticator);
    }

    void handleContext(Context context, HttpClientRequest request) {
        if (StringUtils.hasText(context.getProjectId())) {
            request.header(Constants.HEADER_PROJECT, context.getProjectId());
        }

        if (StringUtils.hasText(context.getToken())) {
            request.header(Constants.HEADER_AUTHORIZATION, context.getToken());
        }

        if (!CollectionUtils.isEmpty(context.getHeaders())) {
            context.getHeaders().forEach(request::header);
        }

        if (context.getTimeout() != null) {
            request.responseTimeout(context.getTimeout());
        }
    }

    <T> Mono<ResponseDTO<T>> handleResponse(HttpClientResponse response, ByteBufMono bodyFlux, Class<T> clazz) {
        int statusCode = response.status().code();
        if (statusCode >= 200 && statusCode < 300) {
            if (clazz == Void.class) {
                return Mono.just(new ResponseDTO<>(true, 0, 200, "OK", "", null));
            }

            HttpHeaders headers = response.responseHeaders();
            return bodyFlux.asString(StandardCharsets.UTF_8).flatMap(body -> {
                int count = 0;
                String headerCount = headers.get(Constants.HEADER_COUNT);
                if (StringUtils.hasText(headerCount)) {
                    count = Integer.parseInt(headerCount);
                }

                if (!StringUtils.hasText(body)) {
                    return Mono.just(new ResponseDTO<T>(true, 0, statusCode, "OK", "", null));
                }

                // 如果返回值是 String 类型, 则直接返回字符串
                if (clazz == String.class) {
                    return Mono.just(new ResponseDTO<T>(true, count, statusCode, "OK", "", clazz.cast(body)));
                }

                T result = CustomGson.GSON.fromJson(body, clazz);
                return Mono.just(new ResponseDTO<T>(true, count, statusCode, "OK", "", result));
            });
        }

        return bodyFlux.asString(StandardCharsets.UTF_8).flatMap(body -> {
            if (!StringUtils.hasText(body)) {
                return Mono.just(new ResponseDTO<>(false, 0, statusCode, "未知原因", "响应体为空", null));
            }

            ResponseDTO<T> responseDTO = (ResponseDTO<T>)CustomGson.GSON.fromJson(body, ResponseDTO.class);
            if (responseDTO == null) {
                return Mono.just(new ResponseDTO<>(false, 0, statusCode, "未知原因", body, null));
            }

            return Mono.just(responseDTO);
        });
    }

    String handleUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("url cannot be empty");
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        if (url.startsWith("/")) {
            return this.baseUrl + url;
        }

        return this.baseUrl + "/" + url;
    }

    ByteBufFlux createRequestBody(Object body) {
        if (body instanceof String) {
            return ByteBufFlux.fromString(Mono.just(String.valueOf(body)));
        } else if (body instanceof byte[]) {
            return ByteBufFlux.fromString(Mono.just(new String((byte[]) body, StandardCharsets.UTF_8)));
        } else {
            byte[] bodyBytes = CustomGson.GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
            return ByteBufFlux.fromString(Mono.just(new String(bodyBytes, StandardCharsets.UTF_8)));
        }
    }

    /**
     * 发送 GET 请求
     *
     * @param url   请求路径
     * @param clazz 请求结果的类型
     * @param <T>   请求结果的类型泛型
     * @return 请求结果
     * @throws IllegalArgumentException 如果 url 为空
     * @throws RequestFailedException   如果请求失败
     */
    public <T> ResponseDTO<T> get(String url, Class<T> clazz) {
        return this.get(Context.EMPTY, url, clazz);
    }

    /**
     * 发送 POST 请求
     *
     * @param url   请求路径
     * @param body  请求体
     * @param clazz 请求结果的类型
     * @param <B>   请求体的类型泛型
     * @param <T>   请求结果的类型泛型
     * @return 请求结果
     * @throws IllegalArgumentException 如果 url 或 body 为空
     * @throws RequestFailedException   如果请求失败
     */
    public <B, T> ResponseDTO<T> post(String url, B body, Class<T> clazz) {
        return this.post(Context.EMPTY, url, body, clazz);
    }

    /**
     * 发送 PUT 请求
     *
     * @param url   请求路径
     * @param body  请求体
     * @param clazz 请求结果的类型
     * @param <B>   请求体的类型泛型
     * @param <T>   请求结果的类型泛型
     * @return 请求结果
     * @throws IllegalArgumentException 如果 url 或 body 为空
     * @throws RequestFailedException   如果请求失败
     */
    public <B, T> ResponseDTO<T> put(String url, B body, Class<T> clazz) {
        return this.put(Context.EMPTY, url, body, clazz);
    }

    /**
     * 发送 PATCH 请求
     *
     * @param url   请求路径
     * @param body  请求体
     * @param clazz 请求结果的类型
     * @param <B>   请求体的类型泛型
     * @param <T>   请求结果的类型泛型
     * @return 请求结果
     * @throws IllegalArgumentException 如果 url 或 body 为空
     * @throws RequestFailedException   如果请求失败
     */
    public <B, T> ResponseDTO<T> patch(String url, B body, Class<T> clazz) {
        return this.patch(Context.EMPTY, url, body, clazz);
    }

    /**
     * 发送 DELETE 请求
     *
     * @param url   请求路径
     * @param body  请求体. 可以为 {@code null}
     * @param clazz 请求结果的类型
     * @param <B>   请求体的类型泛型
     * @param <T>   请求结果的类型泛型
     * @return 请求结果
     * @throws IllegalArgumentException 如果 url 为空
     * @throws RequestFailedException   如果请求失败
     */
    public <B, T> ResponseDTO<T> delete(String url, B body, Class<T> clazz) {
        return this.delete(Context.EMPTY, url, body, clazz);
    }

    /**
     * 发送 GET 请求
     *
     * @param context 请求上下文
     * @param url     请求路径
     * @param clazz   请求结果的类型
     * @param <T>     请求结果的类型泛型
     * @return 请求结果
     * @throws RequestFailedException 如果请求失败
     */
    public <T> ResponseDTO<T> get(Context context, String url, Class<T> clazz) {
        return this.createClient(this.connectTimeout)
                .doOnRequest((req, conn) -> this.handleContext(context, req))
                .get()
                .uri(this.handleUrl(url))
                .responseSingle((resp, respBody) -> this.handleResponse(resp, respBody, clazz))
                .timeout(context.getTimeoutOrDefault(this.callTimeout))
                .block();
    }

    /**
     * 发送 POST 请求
     *
     * @param context 请求上下文
     * @param url     请求路径
     * @param body    请求体
     * @param clazz   请求结果的类型
     * @param <B>     请求体的类型泛型
     * @param <T>     请求结果的类型泛型
     * @return 请求结果
     * @throws RequestFailedException 如果请求失败
     */
    public <B, T> ResponseDTO<T> post(Context context, String url, B body, Class<T> clazz) {
        return this.createClient(this.connectTimeout)
                .doOnRequest((req, conn) -> this.handleContext(context, req))
                .post()
                .uri(this.handleUrl(url))
                .send((req, outbound) -> body == null ? Mono.empty() : outbound.send(this.createRequestBody(body)))
                .responseSingle((resp, respBody) -> this.handleResponse(resp, respBody, clazz))
                .timeout(context.getTimeoutOrDefault(this.callTimeout))
                .block();
    }

    /**
     * 发送 PUT 请求
     *
     * @param context 请求上下文
     * @param url     请求路径
     * @param body    请求体
     * @param clazz   请求结果的类型
     * @param <B>     请求体的类型泛型
     * @param <T>     请求结果的类型泛型
     * @return 请求结果
     * @throws RequestFailedException 如果请求失败
     */
    public <B, T> ResponseDTO<T> put(Context context, String url, B body, Class<T> clazz) {
        return this.createClient(this.connectTimeout)
                .doOnRequest((req, conn) -> this.handleContext(context, req))
                .put()
                .uri(this.handleUrl(url))
                .send((req, outbound) -> body == null ? Mono.empty() : outbound.send(this.createRequestBody(body)))
                .responseSingle((resp, respBody) -> this.handleResponse(resp, respBody, clazz))
                .timeout(context.getTimeoutOrDefault(this.callTimeout))
                .block();
    }

    /**
     * 发送 PATCH 请求
     *
     * @param context 请求上下文
     * @param url     请求路径
     * @param body    请求体
     * @param clazz   请求结果的类型
     * @param <B>     请求体的类型泛型
     * @param <T>     请求结果的类型泛型
     * @return 请求结果
     * @throws IllegalArgumentException 如果请求参数不正确
     * @throws RequestFailedException   如果请求失败
     */
    public <B, T> ResponseDTO<T> patch(Context context, String url, B body, Class<T> clazz) {
        return this.createClient(this.connectTimeout)
                .doOnRequest((req, conn) -> this.handleContext(context, req))
                .patch()
                .uri(this.handleUrl(url))
                .send((req, outbound) -> body == null ? Mono.empty() : outbound.send(this.createRequestBody(body)))
                .responseSingle((resp, respBody) -> this.handleResponse(resp, respBody, clazz))
                .timeout(context.getTimeoutOrDefault(this.callTimeout))
                .block();
    }

    /**
     * 发送 DELETE 请求
     *
     * @param context 请求上下文
     * @param url     请求路径
     * @param body    请求体
     * @param clazz   请求结果的类型
     * @param <B>     请求体的类型泛型
     * @param <T>     请求结果的类型泛型
     * @return 请求结果
     * @throws RequestFailedException 如果请求失败
     */
    public <B, T> ResponseDTO<T> delete(Context context, String url, B body, Class<T> clazz) {
        return this.createClient(this.connectTimeout)
                .doOnRequest((req, conn) -> this.handleContext(context, req))
                .delete()
                .uri(this.handleUrl(url))
                .send((req, outbound) -> body == null ? Mono.empty() : outbound.send(this.createRequestBody(body)))
                .responseSingle((resp, respBody) -> this.handleResponse(resp, respBody, clazz))
                .timeout(context.getTimeoutOrDefault(this.callTimeout))
                .block();
    }
}
