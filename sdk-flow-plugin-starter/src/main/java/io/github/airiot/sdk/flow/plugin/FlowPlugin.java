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

/**
 * 流程插件接口
 */
public interface FlowPlugin<Request> {

    /**
     * 获取插件名称
     *
     * @return 插件名称
     */
    String getName();

    /**
     * 插件类型
     *
     * @return 插件类型
     */
    FlowPluginType getPluginType();

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
     * 当流程插件服务启动时执行的操作
     * <br>
     * 可以在此方法中执行一些初始化操作, 如建立连接, 初始化对象等.
     * <br>
     * 注: 该方法只会调用一次, 并且在 {@link #onConnectionStateChange(boolean)} 之前执行
     */
    default void onStart() {

    }

    /**
     * 当流程插件服务停止时执行的操作
     * <br>
     * 可以在此方法中执行一些清理操作, 如关闭连接等.
     * <br>
     * 注: 该方法只会调用一次.
     */
    default void onStop() {

    }

    /**
     * 执行流程引擎的请求并返回处理结果.
     * <br>
     * 如果抛出异常则视为请求执行失败. 否则视为请求执行成功.
     *
     * @param task 任务信息
     * @return 请求处理结果. 返回结果必须为 Map 或 Object, 该结果可以被后续节点使用
     * @throws FlowPluginException 如果请求执行失败
     */
    Object execute(FlowTask<Request> task) throws FlowPluginException;
}
