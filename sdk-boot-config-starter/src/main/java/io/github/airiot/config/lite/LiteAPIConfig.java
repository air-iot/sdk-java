package io.github.airiot.config.lite;

public class LiteAPIConfig {

    public static final String PREFIX = "api";

    /**
     * 是否为 lite 模式
     */
    private Boolean liteMode;
    /**
     * http 网关地址. 例如: <pre>http://127.0.0.1:3030/rest</pre>
     */
    private String gateway;
    /**
     * grpc 网关地址
     */
    private String gatewayGrpc;
    /**
     * 认证类型
     * <br>
     * project: 项目级认证
     * <br>
     * tenant: 租户级认证
     */
    private String type;
    /**
     * 项目ID
     */
    private String projectId;
    private String ak;
    private String sk;

    public Boolean getLiteMode() {
        return liteMode;
    }

    public void setLiteMode(Boolean liteMode) {
        this.liteMode = liteMode;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getGatewayGrpc() {
        return gatewayGrpc;
    }

    public void setGatewayGrpc(String gatewayGrpc) {
        this.gatewayGrpc = gatewayGrpc;
    }

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
}
