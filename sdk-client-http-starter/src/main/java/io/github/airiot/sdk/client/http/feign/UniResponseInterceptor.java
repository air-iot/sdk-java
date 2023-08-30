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

package io.github.airiot.sdk.client.http.feign;

import feign.InvocationContext;
import feign.Response;
import feign.ResponseInterceptor;
import feign.Util;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class UniResponseInterceptor implements ResponseInterceptor {

    public static final UniResponseInterceptor INSTANCE = new UniResponseInterceptor();

    @Override
    public Object aroundDecode(InvocationContext invocationContext) throws IOException {
        Response response = invocationContext.response();
        if (response.status() == 200) {
            Type returnType = invocationContext.returnType();
            if (returnType == Void.class) {
                ResponseOk result = (ResponseOk) invocationContext.decoder().decode(response, ResponseOk.class);
                return new ResponseDTO<>(true, 0, 200, "OK", "", result);
            }

            int count = 0;
            if (response.headers().containsKey(Constants.HEADER_COUNT)) {
                count = response.headers().get(Constants.HEADER_COUNT).stream()
                        .findFirst().map(Integer::parseInt).orElse(0);
            }

            // 如果返回值是 String 类型, 则直接返回字符串
            if (returnType == String.class) {
                String data = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                return new ResponseDTO<>(true, count, 200, "OK", "", data);
            }

            Object result = invocationContext.decoder().decode(response, returnType);
            return new ResponseDTO<>(true, count, 200, "OK", "", result);
        }
        
        ResponseError error = (ResponseError) invocationContext.decoder().decode(response, ResponseError.class);
        if (error == null) {
            return new ResponseDTO<>(false, 0, response.status(), response.reason(), "", null);
        }

        return new ResponseDTO<>(false, 0, response.status(), error.getMessage(), error.getDetail(), error.getField(), null);
    }
}
