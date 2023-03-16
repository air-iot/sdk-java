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

package io.github.airiot.sdk.client.service.core.dto;


/**
 * 系统变量(数据字典)
 */
public class SystemVariable {

    /**
     * 系统变量标识
     */
    private String id;
    /**
     * 系统变量编号
     */
    private String uid;
    /**
     * 系统变量的名称
     */
    private String name;
    /**
     * 系统变量的数据类型
     */
    private String type;
    /**
     * 系统变量的值
     */
    private Object value;

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "SystemVariable{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
