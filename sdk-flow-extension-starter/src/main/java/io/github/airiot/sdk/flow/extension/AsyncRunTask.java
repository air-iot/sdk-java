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

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.github.airiot.sdk.flow.extension.ExtensionResult;
import io.github.airiot.sdk.flow.extension.ExtensionRunRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class AsyncRunTask implements Runnable {

    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(AsyncRunTask.class);
    private final FlowExtensionDelegate delegate;
    private final FlowExtensionHandler.RunHandler call;
    private final ExtensionRunRequest request;

    public AsyncRunTask(FlowExtensionDelegate delegate, FlowExtensionHandler.RunHandler call, ExtensionRunRequest request) {
        this.delegate = delegate;
        this.call = call;
        this.request = request;
    }

    @Override
    public void run() {
        if (logger.isDebugEnabled()) {
            logger.debug("接收到请求, request={}, data={}", request.getRequest(), request.getData().toStringUtf8());
        }

        try {
            Object result = this.delegate.execute(request);
            ByteString data = null;
            if (result != null) {
                data = ByteString.copyFrom(GSON.toJson(result), StandardCharsets.UTF_8);
            }

            this.call.send(ExtensionResult.newBuilder()
                    .setRequest(request.getRequest())
                    .setStatus(true)
                    .setInfo("OK")
                    .setResult(data)
                    .build());
        } catch (Exception e) {
            logger.error("执行扩展节点失败, request: {}, data: {}", request.getRequest(), request.getData().toStringUtf8(), e);
            this.call.send(ExtensionResult.newBuilder()
                    .setRequest(request.getRequest())
                    .setStatus(false)
                    .setInfo("执行异常")
                    .setDetail(e.getMessage())
                    .build());
        }
    }
}
