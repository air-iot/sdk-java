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

package io.github.airiot.sdk.client.http.clients.core;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.form.FormData;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.MediaLibraryClient;
import io.github.airiot.sdk.client.service.core.dto.MkdirDTO;
import io.github.airiot.sdk.client.service.core.dto.UploadFileResult;

import javax.annotation.Nonnull;

public interface MediaLibraryFeignClient extends MediaLibraryClient {

    @RequestLine("POST /core/mediaLibrary/mkdir")
    @Override
    ResponseDTO<Void> mkdir(@Nonnull MkdirDTO mkdir);
    
    @Override
    default ResponseDTO<UploadFileResult> upload(@Nonnull String catalog, @Nonnull String action, @Nonnull String filename, @Nonnull byte[] fileData) {
        FormData formData = new FormData();
        formData.setFileName(filename);
        formData.setData(fileData);
        return this.upload(catalog, action, formData);
    }

    @RequestLine("POST /core/mediaLibrary/upload?catalog={catalog}&action={action}")
    @Headers("Content-Type: multipart/form-data")
    ResponseDTO<UploadFileResult> upload(@Param("catalog") String catalog, @Param("action") String action, @Param("file") FormData file);
}
