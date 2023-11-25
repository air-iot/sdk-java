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

import org.apache.kafka.common.protocol.types.Field;

/**
 * 驱动默认内置模块
 */
public interface DriverModules {

    String HEARTBEAT = "健康检查";
    String START = "启动驱动";
    String RUN = "执行指令";
    String BATCH_RUN = "批量执行指令";
    String WRITE_TAG = "写数据点";
    String WRITE_POINTS = "写数据点值";
    String WRITE_EVENT = "发送事件";
    String SCHEMA = "Schema";
    String DEBUG = "调试";
    String HTTP_PROXY = "Http代理";
    String WARNING = "报警";

}
