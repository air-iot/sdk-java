package io.github.airiot.sdk.driver.ai;

public class WebsocketSubscription {

    /**
     * 订阅实时数据
     */
    public static final String WEBSOCKET_SUBSCRIPTION_REALTIME = "data";
    /**
     * 设备状态数据
     */
    public static final String WEBSOCKET_SUBSCRIPTION_DEVICE_STATUS = "status";

    /**
     * 设备在线状态
     */
    public static final String DEVICE_STATUS_ONLINE = "online";
    /**
     * 设备离线状态
     */
    public static final String DEVICE_STATUS_OFFLINE = "offline";

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
     * 订阅参数
     */
    public static class Subscription {
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

    /**
     * 设备状态订阅
     */
    public static class DeviceStatusMessagePayload {
        /**
         * 设备编号
         */
        private String id;
        /**
         * 设备所属表标识
         */
        private String table;
        /**
         * 设备最后上数时间
         */
        private long lastSeen;
        /**
         * 设备当前状态.
         * <br>
         * online: 在线
         * offline: 离线
         *
         * @see WebsocketSubscription#DEVICE_STATUS_ONLINE
         * @see WebsocketSubscription#DEVICE_STATUS_OFFLINE
         */
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public long getLastSeen() {
            return lastSeen;
        }

        public void setLastSeen(long lastSeen) {
            this.lastSeen = lastSeen;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public DeviceStatusMessagePayload toOnline(long time) {
            this.lastSeen = time;
            this.status = WebsocketSubscription.DEVICE_STATUS_ONLINE;
            return this;
        }

        public DeviceStatusMessagePayload toOffline() {
            this.status = WebsocketSubscription.DEVICE_STATUS_OFFLINE;
            return this;
        }

        public static DeviceStatusMessagePayload of(String tableId, String deviceId) {
            DeviceStatusMessagePayload payload = new DeviceStatusMessagePayload();
            payload.table = tableId;
            payload.id = deviceId;
            payload.lastSeen = 0;
            payload.status = WebsocketSubscription.DEVICE_STATUS_OFFLINE;
            return payload;
        }

        public boolean isOnline() {
            return WebsocketSubscription.DEVICE_STATUS_ONLINE.equals(this.status);
        }
    }

}
