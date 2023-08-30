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
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.algorithm.grpc.algorithm.RunRequest;
import io.github.airiot.sdk.algorithm.grpc.algorithm.RunResult;
import io.grpc.ClientCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class AlgorithmExecuteTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AlgorithmExecuteTask.class);

    private static final Gson GSON = new Gson();

    private final ClientCall<RunResult, RunRequest> call;
    private final String requestId;
    private final Request request;
    private final Callable<Object> task;

    public AlgorithmExecuteTask(ClientCall<RunResult, RunRequest> call, String requestId, Request request, Callable<Object> task) {
        this.call = call;
        this.requestId = requestId;
        this.request = request;
        this.task = task;
    }
    
    @Override
    public void run() {
        try {
            logger.info("开始执行算法: requestId={}, projectId={}, function={}, params={}", requestId, request.getProjectID(), request.getFunction(), request.getInput());
            Object result = this.task.call();
            logger.info("算法执行结果: requestId={}, result: {}", requestId, result);
            this.call.sendMessage(RunResult.newBuilder()
                    .setRequest(requestId)
                    .setMessage(ByteString.copyFromUtf8(GSON.toJson(new Response(200, null, result))))
                    .build());
        } catch (Exception e) {
            logger.error("算法执行异常: requestId={}, projectId={}, function={}, params={}", requestId, request.getProjectID(), request.getFunction(), request.getInput(), e);
            this.call.sendMessage(RunResult.newBuilder()
                    .setRequest(requestId)
                    .setMessage(ByteString.copyFromUtf8(GSON.toJson(new Response(400, e.getMessage()))))
                    .build());
        }
    }
}
