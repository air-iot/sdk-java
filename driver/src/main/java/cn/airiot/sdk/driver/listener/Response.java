package cn.airiot.sdk.driver.listener;

public class Response {
    private String requestId;
    private Result data;

    public Response() {
    }

    public Response(String requestId, Result data) {
        super();
        this.requestId = requestId;
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Result getData() {
        return data;
    }

    public void setData(Result data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "WsResponse [requestId=" + requestId + ", data=" + data + "]";
    }
}
