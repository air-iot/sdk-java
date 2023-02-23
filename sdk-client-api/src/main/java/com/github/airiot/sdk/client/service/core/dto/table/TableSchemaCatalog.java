package com.github.airiot.sdk.client.service.core.dto.table;


/**
 * 表定义分类
 */
public class TableSchemaCatalog {
    /**
     * 分类ID
     */
    private String id;
    /**
     * 分类名称
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
        return "TableSchemaCatalog{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
