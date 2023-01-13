package cn.airiot.sdk.driver.data;

/**
 * 日志发送异常
 */
public class LogSenderException extends RuntimeException {

    /**
     * 设备ID
     */
    private final String deviceId;
    /**
     * 日志等级
     */
    private final String level;
    /**
     * 日志内容
     */
    private final String logMessage;

    public String getDeviceId() {
        return deviceId;
    }

    public String getLevel() {
        return level;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public LogSenderException(String deviceId, String level, String logMessage, String message) {
        super(String.format("send device log failed, reason: %s. Device = %s, Level = %s, Message = %s",
                message, deviceId, level, logMessage));
        this.deviceId = deviceId;
        this.level = level;
        this.logMessage = logMessage;
    }

    public LogSenderException(String deviceId, String level, String logMessage, Throwable cause) {
        super(String.format("send device log failed, reason: %s. Device = %s, Level = %s, Message = %s",
                cause.getMessage(), deviceId, level, logMessage), cause);
        this.deviceId = deviceId;
        this.level = level;
        this.logMessage = logMessage;
    }
}
