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

package io.github.airiot.sdk.client.service.core;

import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;
import io.github.airiot.sdk.client.service.core.dto.MkdirDTO;
import io.github.airiot.sdk.client.service.core.dto.UploadFileResult;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 媒体库客户端
 */
public interface MediaLibraryClient extends PlatformClient {
    
    /**
     * 创建目录
     *
     * @param catalog 目录. 例如: /a/b/c
     * @return 创建目录请求结果
     */
    default ResponseDTO<Void> mkdir(@Nonnull String catalog) {
        int lastIndex = catalog.indexOf("/");
        if (lastIndex < 0) {
            return this.mkdir(new MkdirDTO("", catalog));
        } else {
            return this.mkdir(new MkdirDTO(catalog.substring(0, lastIndex), catalog.substring(lastIndex + 1)));
        }
    }

    /**
     * 创建目录
     *
     * @param dir 目录信息
     * @return 创建目录请求结果
     */
    ResponseDTO<Void> mkdir(@Nonnull MkdirDTO dir);

    /**
     * 上传文件到媒体库
     *
     * @param catalog  上传目录
     * @param action   出重同名文件时执行的动作. 可选值: cover: 覆盖, rename: 文件名自动加1
     * @param filename 文件名
     * @param fileData 文件数据
     * @return 如果上传成功, 则返回该文件的 url
     */
    ResponseDTO<UploadFileResult> upload(@Nonnull String catalog, @Nonnull String action, @Nonnull String filename, @Nonnull byte[] fileData);

    /**
     * 上传文件到媒体库
     *
     * @param catalog 上传目录
     * @param action  出重同名文件时执行的动作. 可选值: cover: 覆盖, rename: 文件名自动加1
     * @param file    文件
     * @return 如果上传成功, 则返回该文件的 url
     * @throws IOException 如果文件不存在
     */
    default ResponseDTO<UploadFileResult> upload(@Nonnull String catalog, @Nonnull String action, @Nonnull File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileData = new byte[fis.available()];
            fis.read(fileData);
            return upload(catalog, action, file.getName(), fileData);
        }
    }
}
