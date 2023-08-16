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
import io.github.airiot.sdk.client.service.core.dto.UploadFileResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpMediaLibraryClientTests {

    @Autowired
    private MediaLibraryClient mediaLibraryClient;

    @Test
    void mkdir() {
        this.mediaLibraryClient.mkdir("/aa/bb/dd");
    }

    @Test
    void upload() {
        ResponseDTO<UploadFileResult> response = this.mediaLibraryClient.upload("aa/bb/dd", "cover", "test1.txt", "upload bytes to media library".getBytes());
        Assertions.assertTrue(response.isSuccess());
        Assertions.assertNotNull(response.getData());
        System.out.println(response.getData());
    }

    @Test
    void uploadFile() throws IOException {
        File tmpFile = File.createTempFile("upload_file_to_media_library", ".txt");
        tmpFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            fos.write("upload file to media library".getBytes(StandardCharsets.UTF_8));
        }
        this.mediaLibraryClient.upload("aa/bb/dd", "cover", tmpFile);
    }
}
