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


import io.github.airiot.sdk.client.context.RequestContext;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.exception.RequestFailedException;
import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.http.feign.ResponseError;
import io.github.airiot.sdk.client.service.AuthorizationClient;
import io.github.airiot.sdk.client.service.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 通用的 HTTP 客户端.
 */
public class HttpCommonClient implements Authenticator {

    private final OkHttpClient httpClient;
    private final AuthorizationClient authorizationClient;
    private final String baseUrl;

    public HttpCommonClient(String baseUrl, AuthorizationClient authorizationClient,
                            Duration connectTimeout, Duration callTimeout, Duration writeTimeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.authorizationClient = authorizationClient;
        //noinspection KotlinInternalInJava
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout)
                .callTimeout(callTimeout)
                .writeTimeout(writeTimeout)
                .authenticator(this).build();
    }

    Call handleContext(Context context, Request.Builder request) {
        if (StringUtils.hasText(context.getProjectId())) {
            request.header(Constants.HEADER_PROJECT, context.getProjectId());
        }

        if (StringUtils.hasText(context.getToken())) {
            request.header(Constants.HEADER_AUTHORIZATION, context.getToken());
        }

        if (!CollectionUtils.isEmpty(context.getHeaders())) {
            context.getHeaders().forEach(request::header);
        }

        Call call = this.httpClient.newCall(request.build());
        if (context.getTimeout() != null) {
            long timeout = context.getTimeout().toMillis();
            if (timeout > 0) {
                call.timeout().timeout(timeout, TimeUnit.MILLISECONDS);
            } else {
                call.timeout().clearTimeout();
            }
        }

        return call;
    }

    <T> ResponseDTO<T> handleResponse(Response response, Class<T> clazz) throws IOException {
        if (response.isSuccessful()) {
            if (clazz == Void.class) {
                return new ResponseDTO<>(true, 0, 200, "OK", "", null);
            }

            ResponseBody body = response.body();
            if (body == null) {
                return new ResponseDTO<>(true, 0, 200, "OK", "", null);
            }

            int count = 0;
            String headerCount = response.header(Constants.HEADER_COUNT);
            if (StringUtils.hasText(headerCount)) {
                count = Integer.parseInt(headerCount);
            }

            // 如果返回值是 String 类型, 则直接返回字符串
            if (clazz == String.class) {
                String data = new String(body.bytes(), StandardCharsets.UTF_8);
                //noinspection unchecked
                return (ResponseDTO<T>) (new ResponseDTO<>(true, count, 200, "OK", "", data));
            }

            T result = CustomGson.GSON.fromJson(body.charStream(), clazz);
            return new ResponseDTO<>(true, count, 200, "OK", "", result);
        }

        ResponseBody body = response.body();
        if (body == null) {
            return new ResponseDTO<>(false, 0, response.code(), "未知原因", "响应体为空", null);
        }

        String bodyStr = new String(body.bytes(), StandardCharsets.UTF_8);
        ResponseError error = CustomGson.GSON.fromJson(bodyStr, ResponseError.class);
        if (error == null) {
            return new ResponseDTO<>(false, 0, response.code(), "未知原因", bodyStr, null);
        }

        return new ResponseDTO<>(false, 0, response.code(), error.getMessage(), error.getDetail(), error.getField(), null);
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

    RequestBody createRequestBody(Object body) {
        if (body instanceof String) {
            return RequestBody.create(((String) body).getBytes(StandardCharsets.UTF_8));
        } else if (body instanceof byte[]) {
            return RequestBody.create((byte[]) body);
        } else {
            return RequestBody.create(CustomGson.GSON.toJson(body).getBytes(StandardCharsets.UTF_8));
        }
    }

    <T> ResponseDTO<T> call(Call call, Class<T> clazz) {
        try {
            return this.handleResponse(call.execute(), clazz);
        } catch (Exception e) {
            throw new RequestFailedException(500, e.getMessage(), "", e);
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
        Request.Builder builder = new Request.Builder().url(this.handleUrl(url)).get();
        return this.call(this.handleContext(context, builder), clazz);
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
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        Request.Builder builder = new Request.Builder().url(this.handleUrl(url)).post(this.createRequestBody(body));
        return this.call(this.handleContext(context, builder), clazz);
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
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        Request.Builder builder = new Request.Builder().url(this.handleUrl(url)).put(this.createRequestBody(body));
        return this.call(this.handleContext(context, builder), clazz);
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
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        Request.Builder builder = new Request.Builder().url(this.handleUrl(url)).patch(this.createRequestBody(body));
        return this.call(this.handleContext(context, builder), clazz);
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
        Request.Builder builder = new Request.Builder().url(this.handleUrl(url));
        if (body != null) {
            builder.delete(this.createRequestBody(body));
        } else {
            builder.delete();
        }
        return this.call(this.handleContext(context, builder), clazz);
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
        // 如果启用了身份认证
        if (RequestContext.isAuthEnabled()) {
            if (response.request().header(Constants.HEADER_AUTHORIZATION) != null) {
                // 如果已经有了 Authorization 头，说明已经认证过了，不需要再次认证
                return response.request();
            }

            Token token = this.authorizationClient.getToken();
            return response.request().newBuilder().header(Constants.HEADER_AUTHORIZATION, token.getToken()).build();
        }
        return response.request();
    }
}
