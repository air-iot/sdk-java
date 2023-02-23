package com.github.airiot.sdk.client.service.core.dto.table;


/**
 * 表定义信息
 */
public class TableSchema {
    /**
     * 表标识
     */
    private String id;
    /**
     * 表标题
     */
    private String title;
    /**
     * 表所属分类信息
     */
    private TableSchemaCatalog catalog;
    /**
     * 创建人
     */
    private TableCreator creator;
    /**
     * 字段定义信息
     */
    private Schema schema;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TableCreator getCreator() {
        return creator;
    }

    public void setCreator(TableCreator creator) {
        this.creator = creator;
    }

    public TableSchemaCatalog getCatalog() {
        return catalog;
    }

    public void setCatalog(TableSchemaCatalog catalog) {
        this.catalog = catalog;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public String toString() {
        return "TableSchema{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", catalog=" + catalog +
                ", schema=" + schema +
                '}';
    }
}
