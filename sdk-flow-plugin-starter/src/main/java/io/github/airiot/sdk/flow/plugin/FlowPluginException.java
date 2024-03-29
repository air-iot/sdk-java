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

public class FlowPluginException extends RuntimeException {

    private final String info;
    private final String details;

    public String getInfo() {
        return info;
    }

    public String getDetails() {
        return details;
    }

    public FlowPluginException(String info, String details) {
        super(String.format("%s, %s", info, details));
        this.info = info;
        this.details = details;
    }

    public FlowPluginException(String info, Throwable cause) {
        super(String.format("%s, %s", info, cause.getMessage()), cause);
        this.info = info;
        this.details = cause.getMessage();
    }

    public FlowPluginException(String info, String details, Throwable cause) {
        super(String.format("%s, %s", info, details), cause);
        this.info = info;
        this.details = details;
    }

}
