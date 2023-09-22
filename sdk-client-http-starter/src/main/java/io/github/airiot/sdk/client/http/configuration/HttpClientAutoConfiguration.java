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
import feign.form.FormEncoder;
import io.github.airiot.sdk.client.http.clients.HttpProjectAuthorizationClientImpl;
import io.github.airiot.sdk.client.http.clients.HttpTenantAuthorizationClientImpl;
import io.github.airiot.sdk.client.http.clients.common.HttpCommonClient;
import io.github.airiot.sdk.client.http.clients.core.*;
import io.github.airiot.sdk.client.http.clients.ds.DataServiceClientImpl;
import io.github.airiot.sdk.client.http.clients.ds.DataServiceFeignClient;
import io.github.airiot.sdk.client.http.clients.spm.SpmProjectFeignClient;
import io.github.airiot.sdk.client.http.clients.spm.SpmUserFeignClient;
import io.github.airiot.sdk.client.http.clients.warn.WarnFeignClient;
import io.github.airiot.sdk.client.http.clients.warn.WarnRuleFeignClient;
import io.github.airiot.sdk.client.http.config.ServiceConfig;
import io.github.airiot.sdk.client.http.config.ServiceType;
import io.github.airiot.sdk.client.http.feign.AuthRequestInterceptor;
import io.github.airiot.sdk.client.http.feign.RequestHeaderInterceptor;
import io.github.airiot.sdk.client.http.feign.UniResponseInterceptor;
import io.github.airiot.sdk.client.interceptor.EnableClientInterceptors;
import io.github.airiot.sdk.client.properties.AuthorizationProperties;
import io.github.airiot.sdk.client.service.AuthorizationClient;
import io.github.airiot.sdk.client.service.core.*;
import io.github.airiot.sdk.client.service.ds.DataServiceClient;
import io.github.airiot.sdk.client.service.spm.ProjectClient;
import io.github.airiot.sdk.client.service.spm.SpmUserClient;
import io.github.airiot.sdk.client.service.warning.WarnClient;
import io.github.airiot.sdk.client.service.warning.WarnRuleClient;
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
    public SpmUserClient spmUserClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                       HttpClientProperties properties) {
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
                .responseInterceptor(UniResponseInterceptor.INSTANCE)
                .target(SpmUserFeignClient.class, properties.getHost());
    }

    @Bean
    public AuthorizationClient authorizationClient(AuthorizationProperties properties, AppClient httpAppClient, SpmUserClient spmUserClient) {
        return AuthorizationProperties.Type.PROJECT.equals(properties.getType()) ?
                new HttpProjectAuthorizationClientImpl(httpAppClient, properties) :
                new HttpTenantAuthorizationClientImpl(spmUserClient, properties);
    }

    @Bean
    public RequestInterceptor authRequestInterceptor(AuthorizationClient authorizationClient) {
        return new AuthRequestInterceptor(authorizationClient);
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
                    .responseInterceptor(UniResponseInterceptor.INSTANCE)
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

        @Bean
        public MediaLibraryClient mediaLibraryClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                                     HttpClientProperties properties, RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.CORE);
            return Feign.builder()
                    .client(client)
                    .encoder(new FormEncoder(encoder))
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
                    .target(MediaLibraryFeignClient.class, properties.getHost());
        }

        @Bean
        public WarnRuleClient warnRuleClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                             HttpClientProperties properties, RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.WARNING);
            return Feign.builder()
                    .client(client)
                    .encoder(new FormEncoder(encoder))
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
                    .target(WarnRuleFeignClient.class, properties.getHost());
        }

        @Bean
        public WarnClient warnClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                     HttpClientProperties properties, RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.WARNING);
            return Feign.builder()
                    .client(client)
                    .encoder(new FormEncoder(encoder))
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
                    .target(WarnFeignClient.class, properties.getHost());
        }
    }


    /**
     * 数据接口服务
     */
    @Configuration
    public static class HttpDataSourceClientConfiguration {
        @Bean
        public DataServiceFeignClient dataServiceFeignClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
                                                             HttpClientProperties properties,
                                                             RequestInterceptor authRequestInterceptor) {
            ServiceConfig serviceConfig = properties.getOrDefault(ServiceType.DATA_SERVICE);
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
                    .target(DataServiceFeignClient.class, properties.getHost());
        }

        @Bean
        public DataServiceClient dataServiceClient(DataServiceFeignClient dataServiceFeignClient) {
            return new DataServiceClientImpl(dataServiceFeignClient);
        }
    }

    /**
     * 空间管理接口服务
     */
    @Configuration
    public static class HttpSpmClientConfiguration {
        @Bean
        public ProjectClient projectClient(Client client, Encoder encoder, Decoder decoder, Contract contract,
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
                    .requestInterceptor(RequestHeaderInterceptor.INSTANCE)
                    .responseInterceptor(UniResponseInterceptor.INSTANCE)
                    .target(SpmProjectFeignClient.class, properties.getHost());
        }
    }

    @Bean
    public HttpCommonClient commonHttpClient(HttpClientProperties properties, AuthorizationClient authorizationClient) {
        ServiceConfig config = properties.getDefaultConfig();
        return new HttpCommonClient(properties.getHost(), authorizationClient,
                config.getConnectTimeout(), config.getReadTimeout(), config.getReadTimeout());
    }
}
