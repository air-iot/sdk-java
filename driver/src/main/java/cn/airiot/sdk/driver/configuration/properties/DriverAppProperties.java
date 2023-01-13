package cn.airiot.sdk.driver.configuration.properties;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;


/**
 * 驱动基础配置
 */
@Validated
@ConfigurationProperties(prefix = "airiot.driver")
public class DriverAppProperties {

    /**
     * 当前驱动实例所属项目ID, 默认由平台注入
     */
    @NotBlank(message = "项目ID不能为空")
    @Value("${project:default}")
    private String projectId;
    /**
     * 驱动的ID
     */
    @NotBlank(message = "驱动ID不能为空")
    private String id;
    /**
     * 驱动的名称
     */
    @NotBlank(message = "驱动名称不能为空")
    private String name;
    /**
     * 驱动实例ID, 该信息由命令行参数 {@code serviceId} 传入
     * <br>
     * 默认使用 UUID 自动生成
     */
    @Value("${serviceId:}")
    private String instanceId;

    private String distributed = "";

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

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

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getDistributed() {
        return distributed;
    }

    public void setDistributed(String distributed) {
        this.distributed = distributed;
    }

    @Override
    public String toString() {
        return "DriverAppProperties{" +
                "projectId='" + projectId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", distributed='" + distributed + '\'' +
                '}';
    }
}
