package cn.airiot.sdk.driver.data.model;


/**
 * 指令执行日志, 驱动上报平台下发的执行执行结果
 * <br>
 *
 * @see cn.airiot.sdk.driver.grpc.driver.DriverServiceGrpc#getCommandLogMethod()
 */
public class RunLog {
    private String serialNo;
    private String status;
    private long time;
    private String desc;

    public RunLog() {
    }

    public RunLog(String serialNo, String status, long time, String desc) {
        this.serialNo = serialNo;
        this.status = status;
        this.time = time;
        this.desc = desc;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "RunLog{" +
                "serialNo='" + serialNo + '\'' +
                ", status='" + status + '\'' +
                ", time=" + time +
                ", desc='" + desc + '\'' +
                '}';
    }
}
