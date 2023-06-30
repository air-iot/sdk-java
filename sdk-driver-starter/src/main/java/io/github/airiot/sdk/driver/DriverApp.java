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

package io.github.airiot.sdk.driver;

import io.github.airiot.sdk.driver.data.DataSender;
import io.github.airiot.sdk.driver.grpc.driver.Debug;
import io.github.airiot.sdk.driver.listener.BatchCmd;
import io.github.airiot.sdk.driver.listener.Cmd;
import io.github.airiot.sdk.driver.model.RunLog;

import java.util.List;
import java.util.Map;


/**
 * 驱动管理接口, 主要实现对驱动运行状态的管理
 * <br>
 *
 * @param <DriverConfig> 驱动配置信息类型, 需要与 schema 中的 {@code settings} 定义一致
 * @param <Command>      指令信息类型, 需要与 schema 中的 {@code commands} 定义一致
 * @param <Tag>          数据点信息类型, 需要与 schema 中的 {@code tags} 定义一致
 */
public interface DriverApp<DriverConfig, Command, Tag> {

    /**
     * 获取驱动的版本号
     *
     * @return 版本号
     */
    String getVersion();

    /**
     * HTTP 请求代理. 可以将驱动中的功能通过 HTTP 暴露到平台.
     * <br>
     * 只有 {@link #supportHttpProxy()} 返回值为 {@code true} 时才生效
     *
     * @param requestType 请求代理
     * @param headers     请求头
     * @param data        请求参数
     * @return 请求处理结果
     */
    default Object httpProxy(String requestType, Map<String, List<String>> headers, byte[] data) {
        throw new UnsupportedOperationException("不支持 HTTP 请求代理");
    }

    /**
     * 是否支持 HTTP 请求代理
     *
     * @return 如果返回 {@code true} 表示启用 HTTP 请求代理, 否则不启用
     */
    default boolean supportHttpProxy() {
        return false;
    }

    /**
     * 启动或重启驱动
     * <br>
     * 当驱动程序启动或与平台重新连接成功后, 会自动调用方法.
     * <br>
     * 注: 当驱动与平台连接断开并重连成功后也会调用该方法, 所以在实现该方法时, 需要处理好之前已经创建的资源. 例如: 已经建立的 TCP 连接等
     *
     * @param config 驱动实例, 模型及资产信息
     */
    void start(DriverConfig config);

    /**
     * 停止驱动
     * <br>
     * 当驱动进程退出时, 会调用该方法, 可以在该法内完成对相关资产的清理工作
     */
    void stop();

    /**
     * 执行平台对设备下发指令
     * <br>
     * 如果有额外需要保存的指令执行信息时, 可通过 {@link DataSender#writeRunLog(RunLog)} 方法上报到平台.
     *
     * @param request 指令信息
     * @return 指令下发结果
     */
    Object run(Cmd<Command> request);

    /**
     * 批量下发指令
     * <br>
     * 如果有额外需要保存的指令执行信息时, 可通过 {@link DataSender#writeRunLog(RunLog)} 方法上报到平台.
     *
     * @param request 指令信息
     * @return 指令下发结果
     */
    Object batchRun(BatchCmd<Command> request);

    /**
     * 向数据点写入数据
     * <br>
     * 有些驱动支持向数据点写入数据, 例如: OPC UA, 即可以从数据点读取数据, 也可以向数据点写入数据.
     * <br>
     * 如果驱动支持该功能, 需要在 schema 的 tags 定义中添加 rw 属性, 并且在数据点中设置为 true.
     * <br>
     * 详情请参考官方档中的 '数据接入驱动配置说明'
     *
     * @param request 指令信息
     * @return 指令下发结果
     */
    Object writeTag(Cmd<Tag> request);

    /**
     * 驱动调试
     * <br>
     * 该功能未实现
     */
    default Debug debug(Debug config) {
        return config;
    }

    /**
     * 获取驱动配置 schema 定义
     * <br>
     * 通常情况下, 可以将 schema 定义写在驱动程序的 resources 目录下, 然后通过 {@link ClassLoader#getResourceAsStream(String)} 方法获取. 示例如下:
     *
     * <pre>
     * try (InputStream stream = this.getClass().getResourceAsStream("/schema.js")) {
     *     if (stream == null) {
     *         throw new IOException("未找到驱动配置文件");
     *     }
     *
     *     byte[] data = new byte[stream.available()];
     *     int n = stream.read(data);
     *     if (n != data.length) {
     *         throw new IllegalStateException("读取驱动配置文件异常, 数据不完整");
     *     }
     *     return new String(data, StandardCharsets.UTF_8);
     * } catch (IOException e) {
     *     throw new IllegalStateException("读取驱动配置文件异常", e);
     * }
     * </pre>
     *
     * @return 表单 schema
     */
    String schema();
}
