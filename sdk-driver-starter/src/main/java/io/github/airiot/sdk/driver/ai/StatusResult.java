package io.github.airiot.sdk.driver.ai;

public class StatusResult {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public StatusResult(String status) {
        this.status = status;
    }

    public static StatusResult of(String status) {
        return new StatusResult(status);
    }
}
