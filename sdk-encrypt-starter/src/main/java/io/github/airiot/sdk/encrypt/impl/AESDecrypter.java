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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

/**
 * AES 解密器.
 */
public class AESDecrypter extends AbstractDecrypter {

    private static final String AES_KEY = "AIRIOTwj578KshNxbz6psbreTCwyYZCH";
    private final SecretKeySpec secretKeySpec;
    
    public AESDecrypter(byte[] key) {
        super("AES(", ")");
        Security.addProvider(new BouncyCastleProvider());
        this.secretKeySpec = new SecretKeySpec(key, "AES");
    }

    public AESDecrypter() {
        this(AES_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected String doDecrypt(String key, String value) throws Exception {
        return this.decrypt(value);
    }

    private String decrypt(String value) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(value);

        byte[] ivBytes = new byte[16];
        System.arraycopy(AES_KEY.getBytes(), 0, ivBytes, 0, ivBytes.length);
        IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, this.secretKeySpec, ivspec);
        return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
