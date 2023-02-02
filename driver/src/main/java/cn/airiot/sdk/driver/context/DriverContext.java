package cn.airiot.sdk.driver.context;


/**
 * 驱动上下文
 */
public class DriverContext {

    /**
     * 当前驱动实例所属项目ID
     */
    private final String projectId;
    /**
     * 当前驱动ID
     */
    private final String driverId;
    /**
     * 当前驱动名称
     */
    private final String driverName;
    /**
     * 当前驱动实例ID
     */
    private final String driverInstanceId;

    private DriverContext(String projectId, String driverId, String driverName, String driverInstanceId) {
        this.projectId = projectId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverInstanceId = driverInstanceId;
    }
}
