package cn.airiot.sdk.client.dto;

/**
 * 统一请求响应数据类
 *
 * @param <T> 响应数据的类型
 */
public class Response<T> {

    /**
     * 请求是否成功标识. 如果为 {@code true} 表明请求成功, 否则请求失败
     */
    private boolean status;
    /**
     * 请求响应码
     */
    private int code;
    /**
     * 响应信息
     */
    private String info;
    /**
     * 详细信息
     */
    private String detail;
    /**
     * 响应数据
     */
    private T data;

    public boolean isStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public String getDetail() {
        return detail;
    }

    public T getData() {
        return data;
    }

    public Response() {}

    public Response(boolean status, int code, String info, String detail, T data) {
        this.status = status;
        this.code = code;
        this.info = info;
        this.detail = detail;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", code=" + code +
                ", info='" + info + '\'' +
                ", detail='" + detail + '\'' +
                ", data=" + data +
                '}';
    }
}
