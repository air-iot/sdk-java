package cn.airiot.sdk.driver.data.model;

import cn.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * 更新设备信息
 *
 * @see DriverServiceGrpc#getUpdateTableDataMethod()
 */
public class UpdateTableDTO {
    /**
     * 工作表ID
     */
    private String table;
    /**
     * 设备编号
     */
    @SerializedName("id")
    private String rowId;
    /**
     * 要更新的属性列表
     * <br>
     * key: 字段名
     * value: 字段值
     */
    @SerializedName("custom")
    private Map<String, Object> fields;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public UpdateTableDTO() {
    }

    public UpdateTableDTO(String table, String rowId, Map<String, Object> fields) {
        this.table = table;
        this.rowId = rowId;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "UpdateTableDTO{" +
                "table='" + table + '\'' +
                ", rowId='" + rowId + '\'' +
                ", fields=" + fields +
                '}';
    }
}
