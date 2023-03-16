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
import io.github.airiot.sdk.driver.model.RunLog;
import io.github.airiot.sdk.driver.grpc.driver.Debug;
import io.github.airiot.sdk.driver.listener.BatchCmd;
import io.github.airiot.sdk.driver.listener.Cmd;


/**
 * 驱动管理接口, 主要实现对驱动运行状态的管理
 * <br>
 * 当平台下发命令时, 会通过调用该接口的相关接口实现对驱动控制
 */
public interface DriverApp<DriverConfig, Command, Tag> {

    /**
     * 启动或重启驱动
     * <br>
     * 当驱动程序启动后并且与平台成功建立连接后, 会自动调用方法.
     * 驱动实现程序, 可在该方法内根据模型和资产信息完成相应初始化工作.
     *
     * @param config 驱动实例, 模型及资产信息
     */
    void start(DriverConfig config);

    /**
     * 停止驱动
     * <br>
     * 当驱动进程正常退出时, 会调用该方法, 可以在该法内完成对相关的清理工作
     */
    void stop();

    /**
     * 执行平台对单个设备下发指令
     * <br>
     * 指令下发结果通过 {@link DataSender#writeRunLog(RunLog)}
     *
     * @param request 指令信息
     * @return 指令下发结果
     */
    Object run(Cmd<Command> request);

    /**
     * 执行平台对多个设备下发指令
     *
     * @param request 指令信息
     * @return 指令下发结果
     */
    Object batchRun(BatchCmd<Command> request);

    /**
     * 向数据点写入数据
     *
     * @param request 指令信息
     * @return 指令下发结果
     */
    Object writeTag(Cmd<Tag> request);

    Debug debug(Debug config);

    /**
     * 获取驱动配置表单 schema
     *
     * @return 表单 schema
     */
    String schema();
}
