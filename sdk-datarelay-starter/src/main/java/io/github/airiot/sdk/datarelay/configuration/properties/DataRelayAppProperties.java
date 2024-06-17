package io.github.airiot.sdk.datarelay.configuration.properties;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@ConfigurationProperties(prefix = "data-relay")
public class DataRelayAppProperties implements InitializingBean, EnvironmentAware {

    private Environment environment;

    /**
     * 当前驱动实例所属项目ID, 默认由平台注入
     */
    @NotBlank(message = "项目ID不能为空")
    @Value("${project:default}")
    private String projectId;

    @NotBlank(message = "服务实例ID不能为空")
    @Value("${instanceId}")
    private String instanceId;

    @NotBlank(message = "服务标识不能为空")
    private String id;
    @NotBlank(message = "服务名称不能为空")
    private String name;

    public @NotBlank(message = "项目ID不能为空") String getProjectId() {
        return projectId;
    }

    public void setProjectId(@NotBlank(message = "项目ID不能为空") String projectId) {
        this.projectId = projectId;
    }

    public @NotBlank(message = "服务实例ID不能为空") String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(@NotBlank(message = "服务实例ID不能为空") String instanceId) {
        this.instanceId = instanceId;
    }

    public @NotBlank(message = "服务标识不能为空") String getId() {
        return id;
    }

    public void setId(@NotBlank(message = "服务标识不能为空") String id) {
        this.id = id;
    }

    public @NotBlank(message = "服务名称不能为空") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "服务名称不能为空") String name) {
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment env = (ConfigurableEnvironment) this.environment;
            Optional<PropertySource<?>> propertySource = env.getPropertySources().stream()
                    .filter(ps -> ps instanceof CommandLinePropertySource)
                    .findAny();
            if (propertySource.isPresent()) {
                PropertySource<?> ps = propertySource.get();
                Object instanceId = ps.getProperty("instanceId");
                Object projectId = ps.getProperty("project");
                if (instanceId != null) {
                    this.instanceId = String.valueOf(instanceId);
                }
                if (projectId != null) {
                    this.projectId = String.valueOf(projectId);
                }
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
