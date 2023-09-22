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

package io.github.airiot.sdk.flow.extension;

import cn.airiot.sdk.client.dubbo.grpc.engine.ExtensionRunRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class FlowExtensionDelegate implements FlowExtension<Object> {

    private final Gson gson = new Gson();
    private final FlowExtension<Object> delegate;
    private final Type requestType;

    public FlowExtensionDelegate(FlowExtension<Object> delegate) {
        this.delegate = delegate;

        Type reqType = null;
        Type[] types = delegate.getClass().getGenericInterfaces();
        for (Type type : types) {
            if (!(type instanceof ParameterizedType) || !FlowExtension.class.equals(((ParameterizedType) type).getRawType())) {
                continue;
            }

            ParameterizedType extensionType = (ParameterizedType) type;
            reqType = extensionType.getActualTypeArguments()[0];
            break;
        }

        if (reqType == null) {
            throw new IllegalArgumentException("cannot get Request or Response type from extension " + delegate.getName());
        }

        this.requestType = reqType;
    }

    @Override
    public String getId() {
        return this.delegate.getId();
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public void onConnectionStateChange(boolean connected) {
        this.delegate.onConnectionStateChange(connected);
    }

    @Override
    public String schema() throws FlowExtensionException {
        return this.delegate.schema();
    }

    @Override
    public Object run(Object request) throws FlowExtensionException {
        return this.delegate.run(request);
    }

    public Object execute(ExtensionRunRequest request) throws FlowExtensionException {
        Object config;
        if (this.requestType == Void.class) {
            config = null;
        } else {
            try {
                config = this.gson.fromJson(request.getData().toStringUtf8(), this.requestType);
            } catch (JsonSyntaxException e) {
                throw new FlowExtensionException("解析节点配置信息失败", e);
            }
        }
        return this.delegate.run(config);
    }
}
