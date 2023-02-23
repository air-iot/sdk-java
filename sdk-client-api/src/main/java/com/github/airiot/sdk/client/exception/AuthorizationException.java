package com.github.airiot.sdk.client.exception;

/**
 * 认证失败异常
 */
public class AuthorizationException extends RequestFailedException {

    public AuthorizationException(int code, String message, String details) {
        super(code, message, details);
    }

    public AuthorizationException(int code, String message, String details, Throwable cause) {
        super(code, message, details, cause);
    }
}
