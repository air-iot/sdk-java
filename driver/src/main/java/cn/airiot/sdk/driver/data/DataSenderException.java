package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.model.Point;

public class DataSenderException extends RuntimeException {

    private final Point point;

    public DataSenderException(Point point, String message) {
        super(message);
        this.point = point;
    }

    public DataSenderException(Point point, String message, Throwable cause) {
        super(message, cause);
        this.point = point;
    }
}
