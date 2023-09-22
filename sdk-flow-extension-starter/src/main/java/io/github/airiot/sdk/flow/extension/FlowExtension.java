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

/**
 * 流程扩展节点接口
 */
public interface FlowExtension<Request> {

    /**
     * 获取扩展节点标识
     * <br>
     * 节点标识在整个平台中必须唯一, 流程引擎根据流程扩展节点的标识来区分不同的节点.
     *
     * @return 节点标识
     */
    String getId();

    /**
     * 扩展节点名称
     *
     * @return 节点名称
     */
    String getName();

    /**
     * 与流程引擎连接状态发生变化时触发
     * <br>
     * 注: 该方法在每次连接状态发生变化时都会被调用
     *
     * @param connected 当前与流程引擎的连接状态. {@code true} 连接已建立, {@code false} 连接已断开
     */
    default void onConnectionStateChange(boolean connected) {

    }

    /**
     * 当流程扩展节点服务启动时执行的操作
     * <br>
     * 可以在此方法中执行一些初始化操作, 如建立连接, 初始化对象等.
     * <br>
     * 注: 该方法只会调用一次, 并且在 {@link #onConnectionStateChange(boolean)} 之前执行
     */
    default void onStart() {

    }

    /**
     * 当流程扩展节点服务停止时执行的操作
     * <br>
     * 可以在此方法中执行一些清理操作, 如关闭连接等.
     * <br>
     * 注: 该方法只会调用一次.
     */
    default void onStop() {

    }

    /**
     * 获取扩展节点的 schema 定义
     * <br>
     * 如果抛出异常则视为请求执行失败. 否则视为请求执行成功.
     *
     * @return schema 定义信息
     * @throws FlowExtensionException 如果请求执行失败
     */
    String schema() throws FlowExtensionException;

    /**
     * 执行请求
     *
     * @param request 请求参数
     * @return 请求执行结果, 必须为可以序列化为 JSON 的对象, 例如: Map, 自定义 Class 等.
     * @throws FlowExtensionException 如果请求执行异常
     */
    Object run(Request request) throws FlowExtensionException;
}
