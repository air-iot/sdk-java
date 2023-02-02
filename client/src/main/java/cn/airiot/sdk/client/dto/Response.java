package cn.airiot.sdk.client.dto;

import cn.airiot.sdk.client.exception.RequestFailedException;

import java.util.function.Supplier;

/**
 * 统一请求响应数据类
 *
 * @param <T> 响应数据的类型
 */
public class Response<T> {

    /**
     * 请求是否成功标识. 如果为 {@code true} 表明请求成功, 否则请求失败
     */
    private boolean success;
    /**
     * 请求响应码
     */
    private int code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 详细信息
     */
    private String detail;
    /**
     * 响应数据
     */
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public T getData() {
        return data;
    }

    public Response() {
    }

    public Response(boolean success, int code, String message, String detail, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", detail='" + detail + '\'' +
                ", data=" + data +
                '}';
    }

    public String getFullMessage() {
        if (this.detail == null || this.detail.trim().isEmpty()) {
            return this.message;
        }
        return this.message + ". " + this.detail.trim();
    }

    /**
     * 如果请求成功, 则返回 {@link #data}, 否则抛出 {@link RequestFailedException}
     *
     * @return 请求响应数据
     * @throws RequestFailedException 请求失败异常
     */
    public T unwrap() throws RequestFailedException {
        if (isSuccess()) {
            return this.data;
        }
        throw new RequestFailedException(this.code, this.message, this.detail);
    }

    /**
     * 如果请求成功, 则返回 {@link #data}, 否则抛出 {@link E}
     *
     * @param supplier 异常信息
     * @param <E>      自定义异常
     * @return 请求响应数据
     * @throws E 异常类型
     */
    public <E extends RequestFailedException> T unwrap(Supplier<E> supplier) throws E {
        if (isSuccess()) {
            return this.data;
        }
        throw supplier.get();
    }
}
