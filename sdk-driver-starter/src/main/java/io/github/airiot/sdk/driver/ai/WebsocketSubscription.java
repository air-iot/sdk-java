package io.github.airiot.sdk.driver.ai;

public class WebsocketSubscription {

    /**
     * 订阅实时数据
     */
    public static final String WEBSOCKET_SUBSCRIPTION_REALTIME = "data";

    /**
     * 订阅响应
     */
    public static class SubscriptionResponse {
        private boolean success;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public static SubscriptionResponse success() {
            return success("OK");
        }

        public static SubscriptionResponse success(String message) {
            SubscriptionResponse response = new SubscriptionResponse();
            response.success = true;
            response.message = message;
            return response;
        }

        public static SubscriptionResponse failed(String message) {
            SubscriptionResponse response = new SubscriptionResponse();
            response.success = false;
            response.message = message;
            return response;
        }
    }

    /**
     * 实时数据订阅参数
     */
    public static class RealTimeSubscription {
        private String table;
        private String device;

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }
    }

}
