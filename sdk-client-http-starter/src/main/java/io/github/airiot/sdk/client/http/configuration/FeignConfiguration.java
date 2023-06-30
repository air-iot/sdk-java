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

import com.google.gson.Gson;
import feign.*;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.okhttp.OkHttpClient;
import io.github.airiot.sdk.client.http.feign.MyContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * OpenFeign 配置类
 */
@Configuration
public class FeignConfiguration {

    @Bean
    public Client okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public Encoder gsonEncoder() {
        return new Encoder() {
            final Gson gson = new Gson();

            @Override
            public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
                template.body(gson.toJson(object, bodyType));
            }
        };
    }

    @Bean
    public Decoder gsonDecoder() {
        return new Decoder() {
            final Gson gson = new Gson();

            @Override
            public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
                Response.Body body = response.body();
                return this.gson.fromJson(body.asReader(StandardCharsets.UTF_8), type);
            }
        };
    }

    @Bean
    public Contract feignContract() {
        return new MyContract();
    }

}
