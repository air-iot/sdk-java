package io.github.airiot.sdk.client.dto;

/**
 * 平台媒体文件信息
 */
public class MediaFile {

    public static final String MEDIA_LIBRARY_FILE_PROJECT_VARIABLE = "/mediaLibrary__project_id__";

    /**
     * 文件名
     */
    private String name;
    /**
     * 文件地址. 例如: /rest/core/mediaLibrary/{项目ID}/a/b/c.jpg.
     */
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "MediaFile{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
