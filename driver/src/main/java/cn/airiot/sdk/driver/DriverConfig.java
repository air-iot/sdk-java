package cn.airiot.sdk.driver;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 驱动启动配置信息
 */
public class DriverConfig<Config> {

    /**
     * 驱动实例ID
     */
    private String id;
    /**
     * 驱动名称
     */
    private String name;
    /**
     * 驱动类型
     */
    private String driverType;
    /**
     * 驱动实例配置信息
     */
    @SerializedName("device")
    private Config config;
    /**
     * 驱动下模型列表
     */
    private List<Table<Config>> tables;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriverType() {
        return driverType;
    }

    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public List<Table<Config>> getTables() {
        return tables;
    }

    public void setTables(List<Table<Config>> tables) {
        this.tables = tables;
    }
    
    @Override
    public String toString() {
        return "DriverConfig{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", driverType='" + driverType + '\'' +
                ", config=" + config +
                ", tables=" + tables +
                '}';
    }

    public static class Table<Config> {
        private String id;
        /**
         * 模型配置信息
         */
        @SerializedName("device")
        private Config config;
        private List<Device<Config>> devices;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Config getConfig() {
            return config;
        }

        public void setConfig(Config config) {
            this.config = config;
        }

        public List<Device<Config>> getDevices() {
            return devices;
        }

        public void setDevices(List<Device<Config>> devices) {
            this.devices = devices;
        }

        @Override
        public String toString() {
            return "Table{" +
                    "id='" + id + '\'' +
                    ", config=" + config +
                    ", devices=" + devices +
                    '}';
        }
    }

    public static class Device<Config> {
        /**
         * 设备ID
         */
        private String id;
        /**
         * 设备名称
         */
        private String name;
        /**
         * 设备配置信息
         */
        @SerializedName("device")
        private Config config;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Config getConfig() {
            return config;
        }

        public void setConfig(Config config) {
            this.config = config;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", config=" + config +
                    '}';
        }
    }
}
