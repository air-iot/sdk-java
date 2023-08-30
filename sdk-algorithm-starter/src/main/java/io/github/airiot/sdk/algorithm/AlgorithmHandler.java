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

package io.github.airiot.sdk.algorithm;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.algorithm.grpc.algorithm.RunRequest;
import io.github.airiot.sdk.algorithm.grpc.algorithm.RunResult;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

public class AlgorithmHandler extends ClientCall.Listener<RunRequest> {

    private final Logger logger = LoggerFactory.getLogger(AlgorithmHandler.class);

    private final Gson gson = new Gson();
    private final ClientCall<RunResult, RunRequest> call;
    private final AlgorithmApp app;
    private final Map<String, AlgorithmFunctionDefinition> functions;
    private final ThreadPoolExecutor executor;

    public AlgorithmHandler(ClientCall<RunResult, RunRequest> call, AlgorithmApp app, Map<String, AlgorithmFunctionDefinition> functions, ThreadPoolExecutor executor) {
        this.call = call;
        this.app = app;
        this.functions = functions;
        this.executor = executor;
    }

    public void close() {
        this.call.cancel("主动关闭", null);
    }

    /**
     * 创建异步任务
     *
     * @param requestId 请求ID
     * @param request   请求内容
     * @return 异步任务
     */
    private Callable<Object> createTask(String requestId, Request request) {
        String projectId = request.getProjectID();
        String fnName = request.getFunction();

        AlgorithmFunctionDefinition function = functions.get(request.getFunction());
        if (function == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("接收到请求: projectId={}, requestId={}, 未在 {} 类型找到函数 '{}' 定义, 使用默认处理函数",
                        projectId, requestId, this.app.getClass().getName(), request.getFunction());
            }

            return () -> this.app.run(projectId, fnName, request.getInput());
        } else {
            // 如果函数定义中没有请求参数, 则只传递 projectId
            if (function.getRequestType() == Void.class) {
                return () -> function.getCallMethod().invoke(function.getTarget(), projectId);
            } else {
                Object fnParams;
                if (function.getRequestType() == String.class) {
                    fnParams = gson.toJson(request.getInput());
                } else if (function.getRequestType() instanceof Map) {
                    fnParams = request.getInput();
                } else {
                    JsonElement params = gson.toJsonTree(request.getInput());
                    fnParams = gson.fromJson(params, function.getRequestType());
                }
                return () -> function.getCallMethod().invoke(function.getTarget(), projectId, fnParams);
            }
        }
    }

    /**
     * 校验请求是否正确
     *
     * @param request 请求对象
     * @return 如果请求正确, 返回 null, 否则返回错误响应
     */
    private Response validateRequest(RunRequest request) {
        String requestId = request.getRequest();
        String requestData = request.getData().toStringUtf8();

        if (!StringUtils.hasText(requestData)) {
            return new Response(400, "无效的请求参数, 请求内容为空");
        }

        Request req;
        try {
            req = gson.fromJson(request.getData().toStringUtf8(), Request.class);
        } catch (Exception e) {
            logger.warn("接收到请求: requestId={}, 解析请求内容失败", requestId, e);
            return new Response(400, "解析请求内容失败, " + e.getMessage());
        }

        if (!StringUtils.hasText(req.getProjectID())) {
            return new Response(400, "无效的请求参数, 请求内容中未指定项目ID");
        }

        if (!StringUtils.hasText(req.getFunction())) {
            return new Response(400, "无效的请求参数, 请求内容中未指定函数名");
        }

        return null;
    }

    @Override
    public void onMessage(RunRequest request) {
        String requestId = request.getRequest();
        String requestData = request.getData().toStringUtf8();

        logger.info("接收到请求: requestId={}, requestData={}", requestId, requestData);

        Response response = this.validateRequest(request);
        if (response != null) {
            logger.warn("接收到请求: requestId={}, 请求内容校验失败, {}", requestId, response.getError());
            this.call.sendMessage(RunResult.newBuilder()
                    .setRequest(requestId)
                    .setMessage(ByteString.copyFromUtf8(gson.toJson(response)))
                    .build());
            return;
        }

        Request req = null;
        try {
            req = gson.fromJson(requestData, Request.class);
        } catch (JsonSyntaxException e) {
            logger.warn("接收到请求: requestId={}, 解析请求内容失败", requestId, e);
            this.call.sendMessage(RunResult.newBuilder()
                    .setRequest(requestId)
                    .setMessage(ByteString.copyFromUtf8(gson.toJson(new Response(400, "解析请求内容失败, " + e.getMessage()))))
                    .build());
            return;
        }

        this.executor.execute(new AlgorithmExecuteTask(this.call, requestId, req, this.createTask(requestId, req)));
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        logger.warn("算法程序已关闭");
    }

    @Override
    public void onReady() {
        logger.info("算法程序已就绪");
    }
}
