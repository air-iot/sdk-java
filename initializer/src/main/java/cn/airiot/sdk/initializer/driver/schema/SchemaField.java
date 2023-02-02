package cn.airiot.sdk.initializer.driver.schema;

/**
 * 属性基础信息
 */
public class SchemaField {
    /**
     * 属性名
     */
    private String name;
    /**
     * 属性标题
     */
    private String title;
    /**
     * 属性描述
     */
    private String description;
    /**
     * 属性类型
     */
    private String type;
    /**
     * 字段类型
     */
    private String fieldType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}
