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

package io.github.airiot.sdk.client.context;

import io.github.airiot.sdk.client.annotation.DisableAuth;

public class ContextData {
    /**
     * 项目ID
     */
    protected String projectId;
    /**
     * 是否禁用身份认证. <br>
     * 不要手动设置该参数. 应该通过 {@link DisableAuth} 注解控制
     * <br>
     * {@code true}: 禁用认证
     * <br>
     * {@code false}: 启用认证
     */
    protected boolean disableAuth = false;
    /**
     * 是否需要传递项目ID
     * <br>
     * {@code true}: 需要. 此时 {@link #projectId} 不能为空, 否则抛出异常. <br>
     * {@code false}: 不需要. 此时即使 {@link #projectId} 不为空, 也不会传递该信息
     */
    protected boolean takeProject = true;

    public ContextData() {
    }

    public ContextData(ContextData copyFrom) {
        this.projectId = copyFrom.projectId;
    }

}
