package io.github.airiot.config.etcd;


import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * AIRIOT 配置
 */
public class AiriotConfig {

    @SerializedName("App")
    private App app;

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiriotConfig config = (AiriotConfig) o;
        return Objects.equals(app, config.app);
    }

    @Override
    public int hashCode() {
        return Objects.hash(app);
    }

    @Override
    public String toString() {
        return "AiriotConfig{" +
                "app=" + app +
                '}';
    }

    public static class App {
        @SerializedName("API")
        private API api;

        public API getApi() {
            return api;
        }

        public void setApi(API api) {
            this.api = api;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            App app = (App) o;
            return Objects.equals(api, app.api);
        }

        @Override
        public int hashCode() {
            return Objects.hash(api);
        }

        @Override
        public String toString() {
            return "App{" +
                    "api=" + api +
                    '}';
        }
    }

    /**
     * API 配置
     */
    public static class API {
        /**
         * 身份认证类型
         */
        @SerializedName("Type")
        private String type;
        @SerializedName("ProjectId")
        private String projectId;
        /**
         * 应用授权 key
         */
        @SerializedName("AK")
        private String ak;
        /**
         * 应用授权密钥
         */
        @SerializedName("SK")
        private String sk;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getAk() {
            return ak;
        }

        public void setAk(String ak) {
            this.ak = ak;
        }

        public String getSk() {
            return sk;
        }

        public void setSk(String sk) {
            this.sk = sk;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            API api = (API) o;
            return Objects.equals(type, api.type) && Objects.equals(projectId, api.projectId) && Objects.equals(ak, api.ak) && Objects.equals(sk, api.sk);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, projectId, ak, sk);
        }

        @Override
        public String toString() {
            return "API{" +
                    "type='" + type + '\'' +
                    ", projectId='" + projectId + '\'' +
                    ", ak='" + ak + '\'' +
                    ", sk='" + sk + '\'' +
                    '}';
        }
    }
}
