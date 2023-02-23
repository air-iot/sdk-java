package com.github.airiot.sdk.client.exception;


/**
 * 平台内部异常基础类
 */
public class PlatformException extends RuntimeException {

    public PlatformException() {
    }

    public PlatformException(String message) {
        super(message);
    }

    public PlatformException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlatformException(Throwable cause) {
        super(cause);
    }

    public PlatformException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
