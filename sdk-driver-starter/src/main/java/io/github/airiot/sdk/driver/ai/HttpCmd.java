package io.github.airiot.sdk.driver.ai;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

public class HttpCmd {
    /**
     * 表标识
     */
    private String table;
    /**
     * 设备编号
     */
    private String id;
    /**
     * 指令名称
     */
    private String name;
    /**
     * 驱动指令配置
     */
    private List<JsonElement> ops;
    /**
     * 数据写入
     */
    private Map<String, Object> params;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JsonElement> getOps() {
        return ops;
    }

    public void setOps(List<JsonElement> ops) {
        this.ops = ops;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
