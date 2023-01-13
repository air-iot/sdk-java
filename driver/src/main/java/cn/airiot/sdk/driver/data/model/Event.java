package cn.airiot.sdk.driver.data.model;

import cn.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;

/**
 * 事件
 * <br>
 *
 * @see DriverServiceGrpc#getEventMethod()
 */
public class Event {
    /**
     * 资产ID
     */
    private String id;
    /**
     * 表ID
     */
    private String table;
    /**
     * 事件ID
     */
    private String eventId;
    /**
     * 事件产生的时间(时间戳)
     */
    private long time;
    /**
     * 事件数据
     */
    private Object data;

    public Event() {
    }

    public Event(String id, String eventId, long time, Object data) {
        this.id = id;
        this.eventId = eventId;
        this.time = time;
        this.data = data;
    }

    public Event(String id, String table, String eventId, long time, Object data) {
        this.id = id;
        this.table = table;
        this.eventId = eventId;
        this.time = time;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", table='" + table + '\'' +
                ", eventId='" + eventId + '\'' +
                ", time=" + time +
                ", data=" + data +
                '}';
    }
}
