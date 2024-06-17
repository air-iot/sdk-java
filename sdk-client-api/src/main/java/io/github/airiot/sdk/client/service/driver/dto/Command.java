package io.github.airiot.sdk.client.service.driver.dto;


import com.google.gson.annotations.SerializedName;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * 指令
 */
public class Command {
    /**
     * 表标识.
     * <br>
     * 设备所属表的标识
     */
    @SerializedName("table")
    private final String tableId;
    /**
     * 设备编号
     */
    @SerializedName("tableData")
    private final String deviceId;
    /**
     * 指令名称
     */
    private final String name;
    /**
     * 指令参数
     */
    private Object params;

    public String getTableId() {
        return tableId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public Object getParams() {
        return params;
    }

    public Command(String tableId, String deviceId, String name) {
        Assert.hasText(tableId, "表标识不能为空");
        Assert.hasText(deviceId, "设备编号不能为空");
        Assert.hasText(name, "指令名称不能为空");
        this.tableId = tableId;
        this.deviceId = deviceId;
        this.name = name;
    }

    public void setParams(Object params) {
        this.params = Collections.singletonMap(this.name, params);
    }

    @Override
    public String toString() {
        return "Command{" +
                "tableId='" + tableId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", params=" + params +
                '}';
    }
}
