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

package io.github.airiot.sdk.client.http.configuration;


import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import io.github.airiot.sdk.client.http.clients.HttpProjectAuthorizationClientImpl;
import io.github.airiot.sdk.client.http.clients.HttpTenantAuthorizationClientImpl;
import io.github.airiot.sdk.client.http.clients.core.*;
import io.github.airiot.sdk.client.http.clients.spm.SpmUserFeignClient;
import io.github.airiot.sdk.client.http.config.ServiceConfig;
import io.github.airiot.sdk.client.http.config.ServiceType;
import io.github.airiot.sdk.client.http.feign.AuthRequestInterceptor;
import io.github.airiot.sdk.client.http.feign.RequestHeaderInterceptor;
import io.github.airiot.sdk.client.http.feign.UniResponseInterceptor;
import io.github.airiot.sdk.client.interceptor.EnableClientInterceptors;
import io.github.airiot.sdk.client.properties.AuthorizationProperties;
import io.github.airiot.sdk.client.service.AuthorizationClient;
import io.github.airiot.sdk.client.service.core.*;
import io.github.airiot.sdk.client.service.spm.SpmUserClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

@EnableClientInterceptors
@EnableAspectJAutoProxy
@Configuration
@Import(FeignConfiguration.class)
@EnableConfigurationProperties({HttpClientProperties.class, AuthorizationProperties.class})
public class HttpClientAutoConfiguration {

    @Bean
    public RequestInterceptor authRequestInterceptor(AuthorizationClient authorizationClient) {
        return new AuthRequestInterceptor(authorizationClient);
    }

    /**
     * 项目级授权
     */
    @ConditionalOnProperty(prefix = "airiot.client.authorization", name = "type", havingValue = "project", matchIfMissing = true)
    @Configuration
    public static class ProjectAuthorizationConfiguration {

        @Bean
        public AppClient appClient(Client client, Encoder encoder, Decoder decoder, Contract contract, HttpClientProperties properties) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder().client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .responseInterceptor(UniResponseInterceptor.INSTANCE)
                    .target(AppFeignClient.class, properties.getHost());
        }

        @Bean
        public AuthorizationClient projectAuthorizationClient(AppClient httpAppClient, AuthorizationProperties properties) {
            return new HttpProjectAuthorizationClientImpl(httpAppClient, properties.getAppKey(), properties.getAppSecret());
        }
    }

    /**
     * 租户级授权
     */
    @ConditionalOnProperty(prefix = "airiot.client.authorization", name = "type", havingValue = "tenant")
    @Configuration
    public static class TenantAuthorizationConfiguration {

        @Bean
        public SpmUserClient spmUserClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                           HttpClientProperties properties,
                                           RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.SPM);
            return Feign.builder().client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(authRequestInterceptor)
                    .responseInterceptor(UniResponseInterceptor.INSTANCE)
                    .target(SpmUserFeignClient.class, properties.getHost());
        }

        @Bean
        public AuthorizationClient tenantAuthorizationClient(SpmUserClient spmUserClient,
                                                             AuthorizationProperties properties) {
            return new HttpTenantAuthorizationClientImpl(spmUserClient, properties.getAppKey(), properties.getAppSecret());
        }
    }

    /**
     * 核心服务客户端
     */
    @Configuration
    public static class HttpCoreClientConfiguration {

        @Bean
        public DepartmentClient departmentClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                                 HttpClientProperties properties,
                                                 RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder().client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(authRequestInterceptor)
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .responseInterceptor(UniResponseInterceptor.INSTANCE)
                    .target(DepartmentFeignClient.class, properties.getHost());
        }

        @Bean
        public RoleClient roleClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                     HttpClientProperties properties,
                                     RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder().client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(authRequestInterceptor)
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .responseInterceptor(UniResponseInterceptor.INSTANCE)
                    .target(RoleFeignClient.class, properties.getHost());
        }

        @Bean
        public SystemVariableClient systemVariableClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                                         HttpClientProperties properties,
                                                         RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder().client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(authRequestInterceptor)
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .responseInterceptor(UniResponseInterceptor.INSTANCE)
                    .target(SystemVariableFeignClient.class, properties.getHost());
        }

        @Bean
        public TableSchemaClient tableSchemaClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                                   HttpClientProperties properties,
                                                   RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder().client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(authRequestInterceptor)
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .responseInterceptor(UniResponseInterceptor.INSTANCE)
                    .target(TableSchemaFeignClient.class, properties.getHost());
        }

        @Bean
        public TableDataFeignClient tableDataClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                                    HttpClientProperties properties,
                                                    RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder()
                    .client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(authRequestInterceptor)
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .target(TableDataFeignClient.class, properties.getHost());
        }

        @Bean
        public TableDataClientFactory tableDataClientFactory(TableDataFeignClient tableDataFeignClient) {
            return new TableDataClientFactoryImpl(tableDataFeignClient);
        }

        @Bean
        public UserClient userClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                     HttpClientProperties properties,
                                     RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder()
                    .client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(authRequestInterceptor)
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .target(UserFeignClient.class, properties.getHost());
        }

        @Bean
        public TimingDataClient timingDataClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                                 HttpClientProperties properties,
                                                 RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder()
                    .client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .options(new Request.Options(
                            serviceConfig.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            serviceConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                            false
                    ))
                    .requestInterceptor(authRequestInterceptor)
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .target(TimingDataFeignClient.class, properties.getHost());
        }
    }
}
