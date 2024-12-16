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

package io.github.airiot.sdk.flow.plugin.execute;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.flow.configuration.FlowPluginProperties;
import io.github.airiot.sdk.flow.plugin.*;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * 流程插件执行处理器
 */
public class FlowPluginExecuteHandler extends ClientCall.Listener<FlowRequest> {

    private final Logger logger = LoggerFactory.getLogger(FlowPluginExecuteHandler.class);

    private final Gson gson = new Gson();

    private final ClientCall<FlowResponse, FlowRequest> call;
    private final FlowPluginDelegate plugin;
    private final FlowPluginClosedListener listener;

    private final String name;
    private final String mode;

    private final Duration sendTimeout;
    private final BlockingQueue<FlowResponse> queue;

    private Thread sendResultThread;

    public FlowPluginExecuteHandler(FlowPluginProperties properties, ClientCall<FlowResponse, FlowRequest> call, FlowPluginDelegate plugin, FlowPluginClosedListener listener) {
        this.call = call;
        this.plugin = plugin;
        this.name = plugin.getName();
        this.mode = plugin.getPluginType().getType();
        this.listener = listener;
        this.sendTimeout = properties.getSendTimeout();
        this.queue = new LinkedBlockingQueue<>(properties.getQueueSize());
        this.sendResultThread = new Thread(this::handleResult, "send-result");
        this.sendResultThread.setDaemon(true);
        this.sendResultThread.start();
    }

    public void send(FlowResponse result) {
        try {
            if (!this.queue.offer(result, this.sendTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                logger.warn("发送执行结果失败, 队列已满, {}", result.toString());
            }
        } catch (InterruptedException ignore) {
        }
    }

    void handleResult() {
        while (true) {
            FlowResponse result;
            try {
                result = this.queue.poll(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                break;
            }

            if (result == null) {
                continue;
            }

            try {
                this.call.sendMessage(result);
            } catch (Exception e) {
                logger.error("发送执行结果失败, {}", result, e);
            }
        }
    }

    public void close() {
        if (this.sendResultThread != null) {
            this.sendResultThread.interrupt();
            this.sendResultThread = null;
        }

        this.call.cancel("主动关闭", null);
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        logger.warn("流程插件已关闭, name={}, mode={}", name, mode);
        if (status.getCode() != Status.Code.CANCELLED) {
            this.listener.onClose(status, trailers);
        }
    }

    @Override
    public void onReady() {
        logger.info("流程插件已就绪, name={}, mode={}", name, mode);
    }

    @Override
    public void onMessage(FlowRequest request) {
        String config = request.getConfig().toStringUtf8();
        logger.info("流程插件[{}-{}]: 收到请求, project={}, flowId={}, job={}, elementId={}, elementJob={}, config={}",
                name, mode, request.getProjectId(), request.getFlowId(), request.getJob(),
                request.getElementId(), request.getElementJob(), config);

        FlowResponse response = null;
        try {
            FlowTaskResult result = this.plugin.execute(request);
            response = FlowResponse.newBuilder()
                    .setStatus(true)
                    .setInfo(result.getMessage())
                    .setDetail(result.getDetails())
                    .setElementJob(request.getElementJob())
                    .setResult(ByteString.copyFrom(gson.toJson(result.getData()), StandardCharsets.UTF_8))
                    .build();

            logger.info("流程插件[{}-{}]: 处理结果, project={}, flowId={}, job={}, elementId={}, elementJob={}, result={}",
                    name, mode, request.getProjectId(), request.getFlowId(), request.getJob(),
                    request.getElementId(), request.getElementJob(), result);
        } catch (FlowPluginException e) {
            logger.error("流程插件[{}-{}]: 处理请求异常, project={}, flowId={}, job={}, elementId={}, elementJob={}, config={}",
                    name, mode, request.getProjectId(), request.getFlowId(), request.getJob(),
                    request.getElementId(), request.getElementJob(), config, e);

            response = FlowResponse.newBuilder()
                    .setStatus(false)
                    .setInfo(e.getInfo())
                    .setDetail(e.getDetails())
                    .setElementJob(request.getElementJob())
                    .build();
        } catch (Exception e) {
            logger.error("流程插件[{}-{}]: 处理请求异常, project={}, flowId={}, job={}, elementId={}, elementJob={}, config={}",
                    name, mode, request.getProjectId(), request.getFlowId(), request.getJob(),
                    request.getElementId(), request.getElementJob(), config, e);

            response = FlowResponse.newBuilder()
                    .setStatus(false)
                    .setInfo(e.getMessage())
                    .setDetail("")
                    .setElementJob(request.getElementJob())
                    .build();
        }
        
        this.send(response);
    }
}
