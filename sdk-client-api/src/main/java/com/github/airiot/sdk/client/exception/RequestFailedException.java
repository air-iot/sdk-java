package com.github.airiot.sdk.client.exception;


/**
 * 请求失败异常
 */
public class RequestFailedException extends PlatformException {

    /**
     * 错误码
     */
    private final int code;
    /**
     * 详细说明
     */
    private final String details;

    public int getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    public RequestFailedException(int code, String message, String details) {
        super(message);
        this.code = code;
        this.details = details;
    }
    
    public RequestFailedException(int code, String message, String details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = details;
    }
}
