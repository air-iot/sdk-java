package cn.airiot.sdk.client.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务配置
 */
public class ServiceConfig {
    
    /**
     * 是否启用该服务. 默认不启用
     * <br>
     * 如果默认配置 {@link ClientProperties#getDefaultConfig()} 中的 {@code enabled} 为 {@code false} 则表示禁用默认配置
     */
    private boolean enabled = false;

    /**
     * 节点选择器, 用于服务调用时筛选符出匹配的服务实例.
     * <br>
     * 每个服务可以配置多个选择器, 按顺序依次进行筛选, 如果当前选择器找到了匹配的服务实例则停止筛选, 否则使用下一个选择器进行筛选
     * <br>
     * 注: 选择器的筛选结果可能为多个服务实例, 最终调用哪个服务实例由负载均衡器选择
     */
    private List<Selector> selectors = new ArrayList<>();
    /**
     * 未找到匹配的服务实例时的执行策略
     */
    private SelectPolicy policy = SelectPolicy.REJECT;
    /**
     * 是否继承默认配置.
     * <br>
     * 当定义了部分服务配置时, 未配置内容是否继续默认配置.
     * 如果为 {@code true} 表示继承, 否则不继承
     */
    private boolean inherit = true;
    /**
     * 重试次数
     */
    private int retries = 1;
    /**
     * 请求超时时间
     */
    private Duration timeout = Duration.ofSeconds(3);

    public ServiceConfig() {
    }

    public ServiceConfig(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Selector> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<Selector> selectors) {
        this.selectors = selectors;
    }

    public SelectPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(SelectPolicy policy) {
        this.policy = policy;
    }

    public boolean isInherit() {
        return inherit;
    }

    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * 通过服务实例带有的元数据信息进行筛选
     */
    public static class Selector {
        /**
         * 要筛选的元数据信息
         * <br>
         * 只有选择器中定义的所有元数据信息全部匹配才会匹配成功. 即: 服务实例的元数据必须包含选择器中定义的所有元数据
         */
        private Map<String, String> metadata = new HashMap<>();

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * 选择器执行策略
     * <br>
     * 当所有选择器都未找到匹配的服务实例时的处理策略
     */
    public enum SelectPolicy {
        /**
         * 拒绝请求
         */
        REJECT,
        /**
         * 随机选择一个服务实例
         */
        RANDOM,
        /**
         * 使用第一个服务实例
         */
        FIRST,
        /**
         * 返回全部服务实例
         */
        ALL,
    }
}
