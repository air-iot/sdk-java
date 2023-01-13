package cn.airiot.sdk.client.context;


import java.util.Map;

/**
 * Rpc 请求上下文
 */
public class RpcContexts {

    /**
     * 上下文内容
     */
    static class ContextData {
        /**
         * 项目ID
         */
        private String projectId;
        /**
         * 服务实例选择器
         */
        private Map<String, String> selector;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public Map<String, String> getSelector() {
            return selector;
        }

        public void setSelector(Map<String, String> selector) {
            this.selector = selector;
        }
    }

    static class Context {
        private final static ThreadLocal<ContextData> RT_CONTEXT = ThreadLocal.withInitial(ContextData::new);

        public static void set(ContextData data) {
            RT_CONTEXT.set(data);
        }

        public static void setProjectId(String projectId) {
            ContextData data = RT_CONTEXT.get();
            if (data == null) {
                data = new ContextData();
            }
            data.setProjectId(projectId);
            RT_CONTEXT.set(data);
        }
    }
}
