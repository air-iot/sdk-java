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

package com.github.airiot.sdk.client.interceptor;

import com.github.airiot.sdk.client.annotation.DisableAuth;
import com.github.airiot.sdk.client.context.RequestContext;
import com.github.airiot.sdk.client.service.PlatformClient;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthInterceptor extends AbstractClientInterceptor {

    public AuthInterceptor() {
        super(DisableAuth.class, PlatformClient.class);
    }
    
    @Override
    public Advice getAdvice() {
        return new MethodInterceptor() {
            @Nullable
            @Override
            public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
                RequestContext.disableAuth();
                try {
                    return invocation.proceed();
                } finally {
                    RequestContext.enableAuth();
                }
            }
        };
    }
}
