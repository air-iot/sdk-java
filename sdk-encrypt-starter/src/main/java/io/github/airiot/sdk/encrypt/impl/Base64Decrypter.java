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

package io.github.airiot.sdk.encrypt.impl;

import io.github.airiot.sdk.encrypt.AbstractDecrypter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 解密器.
 * <br>
 * 要求属性值以 ENC( 开头, ) 结尾, 中间的内容为 Base64 编码的字符串, 如果解密后的字符串最后一个字符为 \n, 则去掉.
 * <br>
 * 例如:
 * <pre>
 * client:
 *   username: admin
 *   password: ENC(ZGVsbDEyMw==)
 * </pre>
 * 最后解密后的属性值为:
 * <pre>
 * client:
 *   username: admin
 *   password: dell123
 * </pre>
 */
public class Base64Decrypter extends AbstractDecrypter {

    public Base64Decrypter() {
        super("ENC(", ")");
    }
    
    @Override
    protected String doDecrypt(String key, String value) throws Exception {
        byte[] data = Base64.getDecoder().decode(value);
        if (data[data.length - 1] == '\n') {
            return new String(data, 0, data.length - 1, StandardCharsets.UTF_8);
        }
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
