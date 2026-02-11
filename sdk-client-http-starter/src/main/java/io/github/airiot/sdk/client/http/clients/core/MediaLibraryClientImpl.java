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

import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.MediaLibraryClient;
import io.github.airiot.sdk.client.service.core.dto.MediaLibrarySaveFileFromUrlParams;
import io.github.airiot.sdk.client.service.core.dto.MkdirDTO;
import io.github.airiot.sdk.client.service.core.dto.UploadFileResult;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import javax.annotation.Nonnull;
import java.io.InputStream;

@HttpExchange
public interface MediaLibraryClientImpl extends MediaLibraryClient {

    @PostExchange("/core/mediaLibrary/mkdir")
    @Override
    ResponseDTO<Void> mkdir(@Nonnull @RequestBody MkdirDTO mkdir);

    @Override
    default ResponseDTO<UploadFileResult> upload(@Nonnull String catalog,
                                                 @Nonnull String action,
                                                 @Nonnull String filename,
                                                 @Nonnull byte[] fileData) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(fileData)).filename(filename);
        return this.upload(catalog, action, builder.build());
    }

    @Override
    default ResponseDTO<UploadFileResult> upload(@NonNull String catalog, @NonNull String action, @NonNull String filename, @NonNull InputStream inputStream) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new InputStreamResource(inputStream)).filename(filename);
        return this.upload(catalog, action, builder.build());
    }

    @PostExchange(
            value = "/core/mediaLibrary/upload",
            contentType = "multipart/form-data"
    )
    ResponseDTO<UploadFileResult> upload(@RequestParam("catalog") String catalog,
                                         @RequestParam("action") String action,
                                         @RequestPart("file") MultiValueMap<String, HttpEntity<?>> file);

    @PostExchange("/core/mediaLibrary/saveFileFromUrl")
    @Override
    ResponseDTO<UploadFileResult> uploadFromUrl(@NonNull @RequestBody MediaLibrarySaveFileFromUrlParams params);
}
