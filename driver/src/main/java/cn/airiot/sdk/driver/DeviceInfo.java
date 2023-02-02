package cn.airiot.sdk.driver;


import cn.airiot.sdk.driver.data.model.Tag;

import java.util.Collections;
import java.util.Map;

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

    private final Map<String, Tag> tags;

    public String getTableId() {
        return tableId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Map<String, Tag> getTags() {
        return tags;
    }

    public DeviceInfo(String tableId, String instanceId) {
        this.tableId = tableId;
        this.instanceId = instanceId;
        this.tags = Collections.emptyMap();
    }

    public DeviceInfo(String tableId, String instanceId, Map<String, Tag> tags) {
        this.tableId = tableId;
        this.instanceId = instanceId;
        this.tags = tags;
    }
}
