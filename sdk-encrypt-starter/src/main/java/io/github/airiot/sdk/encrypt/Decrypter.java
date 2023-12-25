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

package io.github.airiot.sdk.encrypt;

import org.springframework.core.Ordered;

/**
 * 解密器接口定义.
 * <br>
 * 通过实现该接口, 可以自定义解密器, 对属性值进行解密.
 * <br>
 * 可通过 SPI 机制, 注册自定义解密器.
 */
public interface Decrypter extends Ordered {

    /**
     * 判断是否对该属性进行解密
     *
     * @param key   属性的 key
     * @param value 属性的 value
     * @return 如果返回 true, 则对该属性进行解密, 否则不进行解密
     */
    boolean supports(String key, String value);

    /**
     * 对属性值进行解密
     *
     * @param key   属性的 key
     * @param value 属性的 value
     * @return 解密后的属性值
     * @throws Exception 解密失败时抛出异常
     */
    String decrypt(String key, String value) throws Exception;
}
