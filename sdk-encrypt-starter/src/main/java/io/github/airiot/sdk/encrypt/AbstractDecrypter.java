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

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 根据属性值的占位符, 解密属性值.
 * <br>
 * 即处理固定的的前缀及后缀, 如 ENC(), 然后对中间的属性值进行解密.
 */
public abstract class AbstractDecrypter implements Decrypter {

    /**
     * 占位符前缀
     */
    private final String prefix;
    /**
     * 占位符后缀
     */
    private final String suffix;

    public AbstractDecrypter(String prefix, String suffix) {
        Assert.hasText(prefix, "prefix must not be empty");
        Assert.hasText(suffix, "suffix must not be empty");

        this.prefix = prefix.toUpperCase();
        this.suffix = suffix.toUpperCase();
    }

    @Override
    public boolean supports(String key, String value) {
        if (!StringUtils.hasLength(value)) {
            return false;
        }
        value = value.trim().toUpperCase();
        return value.startsWith(this.prefix) && value.endsWith(this.suffix);
    }

    @Override
    public String decrypt(String key, String value) throws Exception {
        value = value.trim();
        String content = value.substring(this.prefix.length(), value.length() - this.suffix.length());
        try {
            return this.doDecrypt(key, content);
        } catch (Exception e) {
            throw new IllegalStateException("decrypt the value '" + value + "' of property '" + key + "' failed", e);
        }
    }

    /**
     * 执行解密操作
     *
     * @param key   属性的 key
     * @param value 去掉前缀及后缀的内容
     * @return 解密后的内容
     * @throws Exception 解密失败
     */
    protected abstract String doDecrypt(String key, String value) throws Exception;
}
