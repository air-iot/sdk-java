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

import cn.airiot.sdk.client.dubbo.grpc.engine.ExtensionResult;
import cn.airiot.sdk.client.dubbo.grpc.engine.ExtensionRunRequest;
import cn.airiot.sdk.client.dubbo.grpc.engine.ExtensionSchemaRequest;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadPoolExecutor;

class FlowExtensionHandler {

    private final ClientCall<ExtensionResult, ExtensionSchemaRequest> schemaCall;
    private final ClientCall<ExtensionResult, ExtensionRunRequest> runCall;

    private final SchemaHandler schemaHandler;
    private final RunHandler runHandler;

    public SchemaHandler getSchemaHandler() {
        return schemaHandler;
    }

    public RunHandler getRunHandler() {
        return runHandler;
    }

    public FlowExtensionHandler(FlowExtensionDelegate extension,
                                ThreadPoolExecutor executor,
                                ClientCall<ExtensionResult, ExtensionSchemaRequest> schemaCall,
                                ClientCall<ExtensionResult, ExtensionRunRequest> runCall) {
        this.schemaCall = schemaCall;
        this.runCall = runCall;
        this.schemaHandler = new SchemaHandler(extension, schemaCall);
        this.runHandler = new RunHandler(extension, executor, runCall);
    }

    public void close() {
        this.schemaCall.cancel("主动关闭", null);
        this.runCall.cancel("主动关闭", null);
    }

    public static class RunHandler extends ClientCall.Listener<ExtensionRunRequest> {
        private final Logger logger;
        private final Gson gson = new Gson();
        private final ThreadPoolExecutor executor;
        private final FlowExtensionDelegate delegate;
        private final ClientCall<ExtensionResult, ExtensionRunRequest> call;

        public RunHandler(FlowExtensionDelegate delegate,
                          ThreadPoolExecutor executor,
                          ClientCall<ExtensionResult, ExtensionRunRequest> call) {
            this.logger = LoggerFactory.getLogger(delegate.getId() + "#run");
            this.executor = executor;
            this.delegate = delegate;
            this.call = call;
        }

        @Override
        public void onReady() {
            logger.info("就绪");
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            logger.error("已关闭, status={}", status);
        }

        @Override
        public void onMessage(ExtensionRunRequest request) {
            this.executor.execute(new AsyncRunTask(this.delegate, this.call, request));
        }
    }

    public static class SchemaHandler extends ClientCall.Listener<ExtensionSchemaRequest> {

        private final Logger logger;
        private final FlowExtensionDelegate delegate;
        private final ClientCall<ExtensionResult, ExtensionSchemaRequest> call;

        public SchemaHandler(FlowExtensionDelegate delegate,
                             ClientCall<ExtensionResult, ExtensionSchemaRequest> call) {
            this.logger = LoggerFactory.getLogger(delegate.getId() + "#schema");
            this.delegate = delegate;
            this.call = call;
        }

        @Override
        public void onReady() {
            logger.info("就绪");
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            logger.error("已关闭, status={}", status);
        }

        @Override
        public void onMessage(ExtensionSchemaRequest message) {
            logger.debug("接收到请求: {}", message.getRequest());
            try {
                String schema = this.delegate.schema();

                if (logger.isDebugEnabled()) {
                    logger.debug("request: {}, schema: {}", message.getRequest(), schema);
                }

                this.call.sendMessage(ExtensionResult.newBuilder()
                        .setRequest(message.getRequest())
                        .setStatus(true)
                        .setInfo("OK")
                        .setDetail("")
                        .setResult(ByteString.copyFrom(schema, StandardCharsets.UTF_8))
                        .build());
            } catch (Exception e) {
                logger.error("获取扩展节点 schema 失败, request: {}", message.getRequest(), e);
                this.call.sendMessage(ExtensionResult.newBuilder()
                        .setRequest(message.getRequest())
                        .setStatus(false)
                        .setInfo("调用 schema 异常")
                        .setDetail(e.getMessage())
                        .build());
            }
        }
    }
}
