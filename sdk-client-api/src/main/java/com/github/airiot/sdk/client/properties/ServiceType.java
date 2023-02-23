package com.github.airiot.sdk.client.properties;

public enum ServiceType {

    /**
     * 核心服务
     */
    CORE("core"),

    /**
     * 数据接口服务
     */
    DATA_SERVICE("data-service"),

    /**
     * 告警服务
     */
    WARNING("warning"),

    /**
     * 空间管理服务
     */
    SPM("spm");

    private final String name;

    public String getName() {
        return name;
    }

    ServiceType(String name) {
        this.name = name;
    }

    public static ServiceType of(String serviceName) {
        return ServiceType.valueOf(serviceName.toUpperCase());
    }
}
