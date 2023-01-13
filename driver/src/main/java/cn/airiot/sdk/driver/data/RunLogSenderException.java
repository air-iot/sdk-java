package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.model.RunLog;


/**
 * 发送运行日志失败异常
 */
public class RunLogSenderException extends RuntimeException {

    private final RunLog runLog;

    public RunLog getRunLog() {
        return runLog;
    }

    public RunLogSenderException(RunLog runLog, String message) {
        super(message);
        this.runLog = runLog;
    }

    public RunLogSenderException(RunLog runLog, String message, Throwable cause) {
        super(message, cause);
        this.runLog = runLog;
    }

    public RunLogSenderException(RunLog runLog, Throwable cause) {
        super(cause);
        this.runLog = runLog;
    }
}
