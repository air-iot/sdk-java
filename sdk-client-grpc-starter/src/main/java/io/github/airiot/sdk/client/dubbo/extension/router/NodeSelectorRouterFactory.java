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

package io.github.airiot.sdk.client.dubbo.extension.router;

import io.github.airiot.sdk.client.dubbo.configuration.properties.DubboClientProperties;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.config.spring.extension.SpringExtensionInjector;
import org.apache.dubbo.rpc.cluster.CacheableRouterFactory;
import org.apache.dubbo.rpc.cluster.Router;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.context.ApplicationContext;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

@Activate(group = CONSUMER)
public class NodeSelectorRouterFactory extends CacheableRouterFactory {

    private final ApplicationContext applicationContext;

    public NodeSelectorRouterFactory(ApplicationModel applicationModel) {
        SpringExtensionInjector springExtensionInjector = SpringExtensionInjector.get(applicationModel);
        this.applicationContext = springExtensionInjector.getContext();
    }
    
    @Override
    protected Router createRouter(URL url) {
        DubboClientProperties properties = this.applicationContext.getBean(DubboClientProperties.class);
        return new NodeSelectorRouter(url, properties);
    }
}
