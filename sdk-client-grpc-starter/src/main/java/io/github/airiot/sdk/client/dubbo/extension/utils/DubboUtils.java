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

package io.github.airiot.sdk.client.dubbo.extension.utils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.RegistryConstants;
import org.springframework.util.StringUtils;

public class DubboUtils {


    /**
     * 从 {@link URL} 中提取服务名
     *
     * @param url 服务URL
     * @return 服务名
     */
    public static String getServiceName(URL url) {
        String service = url.getParameter(RegistryConstants.PROVIDED_BY, "");

        if (!StringUtils.hasText(service)) {
            service = url.getGroup();
        }

        if (!StringUtils.hasText(service)) {
            throw new IllegalArgumentException("订阅服务: 未定义 provided-by 或 group 信息");
        }

        return service;
    }
}
