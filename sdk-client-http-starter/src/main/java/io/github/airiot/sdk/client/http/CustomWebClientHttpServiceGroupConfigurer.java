package io.github.airiot.sdk.client.http;

import io.github.airiot.sdk.client.annotation.DisableAuth;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.dto.Token;
import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.http.clients.WebClientUtils;
import io.github.airiot.sdk.client.http.config.ServiceConfig;
import io.github.airiot.sdk.client.http.config.ServiceType;
import io.github.airiot.sdk.client.http.configuration.HttpClientProperties;
import io.github.airiot.sdk.client.service.AuthorizationClient;
import io.github.airiot.sdk.client.service.Constants;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class CustomWebClientHttpServiceGroupConfigurer implements WebClientHttpServiceGroupConfigurer {

    private final HttpClientProperties properties;
    private final ObjectProvider<AuthorizationClient> authorizationClient;
    private final List<HttpServiceArgumentResolver> customHttpServiceArgumentResolvers;

    public CustomWebClientHttpServiceGroupConfigurer(HttpClientProperties properties,
                                                     ObjectProvider<AuthorizationClient> authorizationClient,
                                                     List<HttpServiceArgumentResolver> customHttpServiceArgumentResolvers) {
        this.properties = properties;
        this.authorizationClient = authorizationClient;
        this.customHttpServiceArgumentResolvers = customHttpServiceArgumentResolvers;
    }

    public CustomQueryMethodArgumentResolver sdkQueryArgumentResolver() {
        return new CustomQueryMethodArgumentResolver();
    }

    public HttpServiceProxyFactory.Builder httpServiceProxyFactoryBuilder(
            WebClient.Builder webClientBuilder,
            CustomQueryMethodArgumentResolver resolver) {
        WebClient webClient = webClientBuilder.build();
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .customArgumentResolver(resolver);
    }

    @Override
    public void configureGroups(@NonNull Groups<WebClient.Builder> groups) {
        this.configDefaults(groups);
        this.configProxyFactories(groups);
    }

    void configDefaults(Groups<WebClient.Builder> groups) {
        ServiceConfig defaultConfig = properties.getDefaultConfig();
        Map<ServiceType, ServiceConfig> serviceConfigs = properties.getServices();
        final String baseUrl = properties.getHost().endsWith("/") ? properties.getHost().substring(0, properties.getHost().length() - 1) : properties.getHost();
        groups.forEachClient((group, clientBuilder) -> {
            // 遍历所有的请求接口, 查找到所有关闭身份认证的请求
            Set<String> disableAuthRequests = new HashSet<>();
            for (Class<?> httpService : group.httpServiceTypes()) {
                for (Method method : httpService.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(DisableAuth.class) && !method.isAnnotationPresent(HttpExchange.class)) {
                        continue;
                    }

                    AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(method, HttpExchange.class, false, false);
                    if (attributes == null) {
                        continue;
                    }

                    String httpMethod = attributes.getString("method");
                    String requestUrl = attributes.getString("url");
                    if (StringUtils.hasText(httpMethod) && StringUtils.hasText(requestUrl)) {
                        String url = baseUrl + requestUrl;
                        try {
                            String requestPath = new URI(url).getPath();
                            disableAuthRequests.add(String.format("%s %s", httpMethod, requestPath));
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(httpService.getName() + "#" + method.getName() + " 的请求定义无效", e);
                        }
                    }
                }
            }

            clientBuilder.baseUrl(properties.getHost());
            clientBuilder.filter(((request, next) -> {
                // 检测是否跳过身份认证
                if (disableAuthRequests.contains(String.format("%s %s", request.method(), request.url().getPath()))) {
                    return next.exchange(request);
                }
                // 身份认证处理
                Token token = Objects.requireNonNull(authorizationClient.getIfUnique()).getToken();
                ClientRequest newRequest = ClientRequest.from(request)
                        .header(Constants.HEADER_AUTHORIZATION, token.getToken())
                        .build();
                return next.exchange(newRequest);
            }));
            clientBuilder.filter(this.requestFailedFilter());
            clientBuilder.filter(this.responseFailedFilter());
            // 确保所有的响应都被能被处理
            // 如果没有这个配置, 非 200 的响应会直接报错
            clientBuilder.defaultStatusHandler(statusCode -> true, resp -> Mono.empty());

            // 定义每个服务的超时配置
            ServiceType serviceType = ServiceType.of(group.name());
            ServiceConfig serviceConfig = serviceConfigs.getOrDefault(serviceType, defaultConfig);

            HttpClient client = HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) serviceConfig.getConnectTimeout().toMillis())
                    .doOnRequest((req, conn) -> {
                        conn.addHandlerLast(new ReadTimeoutHandler((int) serviceConfig.getReadTimeout().toSeconds()));
                    });

            // 如果不是 spm 的相关接口, 在请注时添加项目ID
            if (ServiceType.SPM != serviceType) {
                client = client.doOnRequest(WebClientUtils.proxyProjectId());
            }

            clientBuilder.clientConnector(new ReactorClientHttpConnector(client));
            clientBuilder.codecs(configurer -> configurer.customCodecs()
                    .registerWithDefaultConfig(new CustomHttpMessageReader()));
        });
    }

    void configProxyFactories(Groups<WebClient.Builder> groups) {
        groups.forEachProxyFactory((_, factoryBuilder) -> {
            for (HttpServiceArgumentResolver resolver : this.customHttpServiceArgumentResolvers) {
                factoryBuilder.customArgumentResolver(resolver);
            }
        });
    }

    private ExchangeFilterFunction requestFailedFilter() {
        return ((request, next) -> next.exchange(request)
                .onErrorResume(throwable -> {
                    String respErr = CustomGson.GSON.toJson(new ResponseDTO<>(false, HttpStatus.GATEWAY_TIMEOUT.value(), "请求异常", throwable.getMessage(), null));
                    return Mono.just(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT)
                            .header("Content-Type", "application/json")
                            .body(respErr)
                            .build());
                }));
    }

    private ExchangeFilterFunction responseFailedFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            // 检查状态码是否不为 200 (OK)
            if (!clientResponse.statusCode().is2xxSuccessful()) {
                // 关键点：将 Body 提取出来并封装进异常，或者统一转换
                return clientResponse.bodyToMono(String.class)
                        .defaultIfEmpty("No body content")
                        .flatMap(body -> Mono.just(clientResponse.mutate().body(body).build()));
            }
            // 如果是 2xx，直接返回，不干扰正常解码流程
            return Mono.just(clientResponse);
        });
    }
}
