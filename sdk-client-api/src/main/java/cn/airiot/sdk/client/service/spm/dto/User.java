package cn.airiot.sdk.client.service.spm.dto;

/**
 * 空间管理-租户
 */
public class User {
    /**
     * 用户唯一标识
     */
    private String id;
    /**
     * 用户名称
     */
    private String name;

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

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

