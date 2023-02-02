package cn.airiot.sdk.client.context;

public class ContextData {
    /**
     * 项目ID
     */
    protected String projectId;
    /**
     * 是否禁用身份认证. <br>
     * 不要手动设置该参数. 应该通过 {@link cn.airiot.sdk.client.annotation.DisableAuth} 注解控制
     * <br>
     * {@code true}: 禁用认证
     * <br>
     * {@code false}: 启用认证
     */
    protected boolean disableAuth = false;
    /**
     * 是否需要传递项目ID
     * <br>
     * {@code true}: 需要. 此时 {@link #projectId} 不能为空, 否则抛出异常. <br>
     * {@code false}: 不需要. 此时即使 {@link #projectId} 不为空, 也不会传递该信息
     */
    protected boolean takeProject = true;

    public ContextData() {
    }

    public ContextData(ContextData copyFrom) {
        this.projectId = copyFrom.projectId;
    }

}
