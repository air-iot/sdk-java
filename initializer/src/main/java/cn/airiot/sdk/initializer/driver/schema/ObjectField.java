package cn.airiot.sdk.initializer.driver.schema;


import java.util.List;

/***
 * 对象属性
 */
public class ObjectField extends SchemaField {
    
    private String type = "object";
    /**
     * 对象内的字段列表
     */
    private List<SchemaField> properties;
    /**
     * 必填字段列表
     */
    private List<String> required;
}
