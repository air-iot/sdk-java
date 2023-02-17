package cn.airiot.sdk.client.service.core.dto;


/**
 * 系统变量(数据字典)
 */
public class SystemVariable {

    /**
     * 系统变量标识
     */
    private String id;
    /**
     * 系统变量编号
     */
    private String uid;
    /**
     * 系统变量的名称
     */
    private String name;
    /**
     * 系统变量的数据类型
     */
    private String type;
    /**
     * 系统变量的值
     */
    private Object value;

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "SystemVariable{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
