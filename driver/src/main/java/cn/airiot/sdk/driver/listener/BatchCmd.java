package cn.airiot.sdk.driver.listener;


import java.util.List;

/**
 * 对多个设备下发指令
 *
 * @param <Command> 命令类型
 */
public class BatchCmd<Command> {
    /**
     * 请求标识
     */
    private final String requestId;
    /**
     * 模型ID
     */
    private final String modelId;
    /**
     * 设备ID列表
     */
    private final List<String> deviceIds;
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

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public Command getCommand() {
        return command;
    }

    public BatchCmd(String requestId, String modelId, List<String> deviceIds, String serialNo, Command command) {
        this.requestId = requestId;
        this.modelId = modelId;
        this.deviceIds = deviceIds;
        this.serialNo = serialNo;
        this.command = command;
    }

    @Override
    public String toString() {
        return "BatchCmd{" +
                "requestId='" + requestId + '\'' +
                ", modelId='" + modelId + '\'' +
                ", deviceIds=" + deviceIds +
                ", serialNo='" + serialNo + '\'' +
                ", command=" + command +
                '}';
    }
}
