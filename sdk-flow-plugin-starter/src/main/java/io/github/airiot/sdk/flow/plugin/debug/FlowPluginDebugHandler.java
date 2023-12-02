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

package io.github.airiot.sdk.flow.plugin.debug;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.flow.plugin.*;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


/**
 * 流程插件调试处理器
 */
public class FlowPluginDebugHandler extends ClientCall.Listener<DebugRequest> {

    private final Logger logger = LoggerFactory.getLogger(FlowPluginDebugHandler.class);

    private final Gson gson = new Gson();

    private final ClientCall<DebugResponse, DebugRequest> call;
    private final FlowPluginDelegate plugin;
    private final FlowPluginClosedListener listener;

    private final String name;
    private final String mode;

    public FlowPluginDebugHandler(ClientCall<DebugResponse, DebugRequest> call, FlowPluginDelegate plugin, FlowPluginClosedListener listener) {
        this.call = call;
        this.plugin = plugin;
        this.name = plugin.getName();
        this.mode = plugin.getPluginType().getType();
        this.listener = listener;
    }

    public void close() {
        this.call.cancel("主动关闭", null);
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        logger.warn("流程插件调试流已关闭, name={}, mode={}", name, mode);
        if (status.getCode() != Status.Code.CANCELLED) {
            this.listener.onClose(status, trailers);
        }
    }

    @Override
    public void onReady() {
        logger.info("流程插件调试流已就绪, name={}, mode={}", name, mode);
    }

    @Override
    public void onMessage(DebugRequest request) {
        String config = request.getConfig().toStringUtf8();
        logger.info("流程插件调试[{}-{}]: 收到请求, project={}, flowId={}, elementId={}, config={}",
                name, mode, request.getProjectId(), request.getFlowId(), request.getElementId(), config);

        DebugResponse response = null;
        try {
            DebugResult result = this.plugin.debug(request);
            response = DebugResponse.newBuilder()
                    .setStatus(result.isSuccess())
                    .setInfo(result.getReason())
                    .setDetail(result.getDetail())
                    .setElementJob(request.getElementJob())
                    .setResult(ByteString.copyFrom(gson.toJson(result), StandardCharsets.UTF_8))
                    .build();

            logger.info("流程插件调试[{}-{}]: 处理结果, project={}, flowId={}, elementId={}, result={}",
                    name, mode, request.getProjectId(), request.getFlowId(), request.getElementId(), result);
        } catch (FlowPluginException e) {
            logger.error("流程插件调试[{}-{}]: 处理请求异常, project={}, flowId={}, elementId={}, config={}",
                    name, mode, request.getProjectId(), request.getFlowId(), request.getElementId(), config, e);

            response = DebugResponse.newBuilder()
                    .setStatus(false)
                    .setInfo(e.getInfo())
                    .setDetail(e.getDetails())
                    .setElementJob(request.getElementJob())
                    .build();
        } catch (Exception e) {
            logger.error("流程插件调试[{}-{}]: 处理请求异常, project={}, flowId={}, elementId={}, config={}",
                    name, mode, request.getProjectId(), request.getFlowId(), request.getElementId(), config, e);

            response = DebugResponse.newBuilder()
                    .setStatus(false)
                    .setInfo(e.getMessage())
                    .setDetail("")
                    .setElementJob(request.getElementJob())
                    .build();
        }

        try {
            this.call.sendMessage(response);
            logger.info("流程插件调试[{}-{}]: 处理结果已发送, project={}, flowId={}, elementId={}, elementJob={}",
                    name, mode, request.getProjectId(), request.getFlowId(), request.getElementId(), request.getElementJob());
        } catch (Exception e) {
            logger.error("流程插件调试[{}-{}]: 发送处理结果异常, project={}, flowId={}, elementId={}, config={}, response={}",
                    name, mode, request.getProjectId(), request.getFlowId(),
                    request.getElementId(), config, response, e);
        }
    }
}
