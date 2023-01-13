package cn.airiot.sdk.driver.listener;


/**
 * 对单个设备下发指令
 *
 * @param <Command> 命令类型
 */
public class Cmd<Command> {

    /**
     * 请求标识
     */
    private final String requestId;
    /**
     * 模型ID
     */
    private final String modelId;
    /**
     * 设备ID
     */
    private final String deviceId;
    /**
     * 序号
     */
    private final String serialNo;
    /**
     * 命令
     */
    private final Command command;

    public String getRequestId() {
        return requestId;
    }

    public String getModelId() {
        return modelId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public Command getCommand() {
        return command;
    }

    public Cmd(String requestId, String modelId, String deviceId, String serialNo, Command command) {
        this.requestId = requestId;
        this.modelId = modelId;
        this.deviceId = deviceId;
        this.serialNo = serialNo;
        this.command = command;
    }

    @Override
    public String toString() {
        return "Cmd{" +
                "requestId='" + requestId + '\'' +
                ", modelId='" + modelId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", serialNo='" + serialNo + '\'' +
                ", command=" + command +
                '}';
    }
}
