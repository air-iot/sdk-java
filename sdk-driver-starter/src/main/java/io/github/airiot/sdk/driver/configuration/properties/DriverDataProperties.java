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

package io.github.airiot.sdk.driver.configuration.properties;


import io.github.airiot.sdk.driver.data.DataSenderException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;


/**
 * 驱动采集的数据及事件上报配置
 */
@ConfigurationProperties(prefix = "airiot.driver.data")
public class DriverDataProperties {

    /**
     * 连接断开时的数据处理策略
     */
    private DataHandlePolicyOnConnectLost policy = DataHandlePolicyOnConnectLost.EXCEPTION;
    /**
     * 连接断开时, 输出到日志的等级
     * <br>
     * 只有 {@link #policy} 为 {@link DataHandlePolicyOnConnectLost#LOG} 时有效
     */
    private LogLevel logLevel = LogLevel.ERROR;


    public DataHandlePolicyOnConnectLost getPolicy() {
        return policy;
    }

    public void setPolicy(DataHandlePolicyOnConnectLost policy) {
        this.policy = policy;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }


    /**
     * 连接断开时的数据处理策略
     */
    public enum DataHandlePolicyOnConnectLost {
        /**
         * 抛出 {@link DataSenderException} 异常
         */
        EXCEPTION,
        /**
         * 输出到日志
         */
        LOG,
        /**
         * 丢弃
         */
        DISCARD;
    }
}
