package cn.airiot.sdk.driver.listener;

public class Request {
    private String requestId;
    private String action;
    private Object data;

    public Request() {
    }

    public Request(String requestId, String action, Object data) {
        super();
        this.requestId = requestId;
        this.action = action;
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "WsRequest [requestId=" + requestId + ", action=" + action + ", data=" + data + "]";
    }

}
