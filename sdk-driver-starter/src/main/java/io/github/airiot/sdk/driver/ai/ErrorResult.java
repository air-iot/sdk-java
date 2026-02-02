package io.github.airiot.sdk.driver.ai;

public class ErrorResult {

    private String error;

    public ErrorResult(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static ErrorResult of(String errorMessage) {
        return new ErrorResult(errorMessage);
    }
}
