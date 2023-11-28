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

import io.github.airiot.sdk.flow.plugin.debug.FlowPluginDebugHandler;
import io.github.airiot.sdk.flow.plugin.execute.FlowPluginExecuteHandler;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class FlowPluginHandler {

    private final Logger logger = LoggerFactory.getLogger(FlowPluginHandler.class);

    private final Channel channel;
    private final FlowPluginDelegate plugin;
    private final FlowPluginClosedListener listener;
    private FlowPluginExecuteHandler executeHandler;
    private FlowPluginDebugHandler debugHandler;

    public FlowPluginHandler(Channel channel, FlowPluginDelegate plugin, FlowPluginClosedListener listener) {
        this.channel = channel;
        this.plugin = plugin;
        this.listener = listener;
    }

    Metadata getMetadata() {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("name", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(plugin.getName().getBytes(StandardCharsets.UTF_8))
        );
        metadata.put(
                Metadata.Key.of("mode", Metadata.ASCII_STRING_MARSHALLER),
                Hex.encodeHexString(plugin.getPluginType().getType().getBytes(StandardCharsets.UTF_8))
        );
        return metadata;
    }

    public void start() {
        logger.info("注册插件: name={}, mode={}", plugin.getName(), plugin.getPluginType().getType());
        if (this.executeHandler != null) {
            this.executeHandler.close();
        }

        if (this.debugHandler != null) {
            this.debugHandler.close();
        }

        ClientCall<FlowResponse, FlowRequest> executeCall = channel.newCall(
                PluginServiceGrpc.getRegisterMethod(),
                CallOptions.DEFAULT.withWaitForReady()
        );

        this.executeHandler = new FlowPluginExecuteHandler(executeCall, plugin, this.listener);
        executeCall.start(executeHandler, this.getMetadata());
        executeCall.request(Integer.MAX_VALUE);


        ClientCall<DebugResponse, DebugRequest> debugCall = channel.newCall(
                PluginServiceGrpc.getDebugStreamMethod(),
                CallOptions.DEFAULT.withWaitForReady()
        );

        this.debugHandler = new FlowPluginDebugHandler(debugCall, plugin, this.listener);
        debugCall.start(this.debugHandler, this.getMetadata());
        debugCall.request(Integer.MAX_VALUE);

        logger.info("注册插件: 成功, name={}, mode={}", plugin.getName(), plugin.getPluginType());
    }

    public void stop() {
        logger.info("注销插件: name={}, mode={}", plugin.getName(), plugin.getPluginType().getType());

        if (this.executeHandler != null) {
            this.executeHandler.close();
        }

        if (this.debugHandler != null) {
            this.debugHandler.close();
        }

        logger.info("注销插件: 成功, name={}, mode={}", plugin.getName(), plugin.getPluginType());
    }
}
