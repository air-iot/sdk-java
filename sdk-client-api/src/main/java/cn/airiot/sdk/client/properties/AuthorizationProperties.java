package cn.airiot.sdk.client.properties;


import cn.airiot.sdk.client.context.RequestContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 平台认证配置
 */
@ConfigurationProperties(prefix = "airiot.client.authorization")
public class AuthorizationProperties implements InitializingBean {

    /**
     * 认证类型
     */
    private Type type = Type.PROJECT;
    /**
     * 认证信息
     */
    private String appKey;
    private String appSecret;
    /**
     * 项目ID.
     * <br>
     * 仅在 {@link #type} 为 {@link Type#PROJECT} 时有效.
     * <br>
     * 如果 {@link #type} 为 {@link Type#PROJECT} 时不能为空.
     * <br>
     * 除了配置文件之外, 也可以通过启动参数 {@code --project} 动态设置
     */
    @Value("${project:}")
    private String projectId = "";

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Type.PROJECT.equals(this.type)) {
            if (!StringUtils.hasText(this.projectId)) {
                throw new IllegalArgumentException("设置 airiot.client.authorization.type=PROJECT 时, 必须设置 airiot.client.authorization.project-id");
            }
            RequestContext.setProjectId(this.projectId);
        }
    }

    public enum Type {
        /**
         * 项目级授权
         */
        PROJECT,
        /**
         * 租户级授权
         */
        TENANT;
    }
}
