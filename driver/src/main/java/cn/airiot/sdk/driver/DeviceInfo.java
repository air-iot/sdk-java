package cn.airiot.sdk.driver;


/**
 * 设备基础信息
 */
public class DeviceInfo {

    /**
     * 设备所属表ID
     */
    private final String tableId;
    /**
     * 设备所属驱动实例ID
     */
    private final String instanceId;

    public String getTableId() {
        return tableId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public DeviceInfo(String tableId, String instanceId) {
        this.tableId = tableId;
        this.instanceId = instanceId;
    }
}
