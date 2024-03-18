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

public class MediaLibrarySaveFileFromUrlParams {

    /**
     * 远程文件地址
     */
    private final String fileUrl;
    /**
     * 上传到媒体库的目录
     */
    private final String mediaLibraryPath;
    /**
     * 保存的文件名
     */
    private final String saveFileName;

    public String getFileUrl() {
        return fileUrl;
    }

    public String getMediaLibraryPath() {
        return mediaLibraryPath;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public MediaLibrarySaveFileFromUrlParams(String fileUrl, String mediaLibraryPath, String saveFileName) {
        this.fileUrl = fileUrl;
        this.mediaLibraryPath = mediaLibraryPath;
        this.saveFileName = saveFileName;
    }

    @Override
    public String toString() {
        return "MediaLibrarySaveFileFromUrlParams{" +
                "fileUrl='" + fileUrl + '\'' +
                ", mediaLibraryPath='" + mediaLibraryPath + '\'' +
                ", saveFileName='" + saveFileName + '\'' +
                '}';
    }
}
