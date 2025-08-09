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
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

/**
 * AES 解密器.
 */
public class SM4GCMDecrypter extends AbstractDecrypter {

    public SM4GCMDecrypter() {
        super("SM4_GCM(", ")");
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    protected String doDecrypt(String key, String value) throws Exception {
        return this.decrypt(value);
    }

    private String decrypt(String value) throws Exception {
        String aesKey = System.getenv("AIRIOT_CIPHER_KEY");
        if (aesKey == null || aesKey.trim().isEmpty()) {
            throw new IllegalArgumentException("The AES KEY environment variable is not set or is empty.");
        }
        SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), "SM4");

        byte[] encryptedData = Base64.getDecoder().decode(value);

        byte[] ivBytes = new byte[16];
        byte[] data = new byte[encryptedData.length - ivBytes.length];
        System.arraycopy(encryptedData, 0, ivBytes, 0, ivBytes.length);
        System.arraycopy(encryptedData, ivBytes.length, data, 0, data.length);

        GCMParameterSpec gcpSpec = new GCMParameterSpec(128, ivBytes);

        Cipher cipher = Cipher.getInstance("SM4/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcpSpec);
        return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
