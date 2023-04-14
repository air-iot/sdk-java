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

package io.github.airiot.sdk.flow.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class FlowPluginDelegate implements FlowPlugin<Object> {

    private final Gson gson = new Gson();
    private final FlowPlugin<Object> delegate;
    private final Class<?> requestType;

    public FlowPluginDelegate(FlowPlugin<Object> delegate) {
        this.delegate = delegate;

        Class<?> reqType = null;
        Type[] types = delegate.getClass().getGenericInterfaces();
        for (Type type : types) {
            if (!(type instanceof ParameterizedType) || !FlowPlugin.class.equals(((ParameterizedType) type).getRawType())) {
                continue;
            }

            ParameterizedType pluginType = (ParameterizedType) type;
            reqType = (Class<?>) pluginType.getActualTypeArguments()[0];
            break;
        }

        if (reqType == null) {
            throw new IllegalArgumentException("cannot get Request or Response type from plugin " + delegate.getName());
        }

        this.requestType = reqType;
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public FlowPluginType getPluginType() {
        return this.delegate.getPluginType();
    }

    @Override
    public void onConnectionStateChange(boolean connected) {
        this.delegate.onConnectionStateChange(connected);
    }

    @Override
    public FlowTaskResult execute(FlowTask<Object> request) throws FlowPluginException {
        return this.delegate.execute(request);
    }

    public FlowTaskResult execute(FlowRequest request) throws FlowPluginException {
        Object config;
        if (this.requestType == Void.class) {
            config = null;
        } else {
            try {
                config = this.gson.fromJson(request.getConfig().toStringUtf8(), this.requestType);
            } catch (JsonSyntaxException e) {
                throw new FlowPluginException("解析节点配置信息失败", e);
            }
        }
        return this.delegate.execute(new FlowTask<>(
                request.getProjectId(), request.getFlowId(), request.getJob(),
                request.getElementId(), request.getElementJob(), config
        ));
    }
}
