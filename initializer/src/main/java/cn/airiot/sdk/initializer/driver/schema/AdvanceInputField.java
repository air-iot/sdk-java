package cn.airiot.sdk.initializer.driver.schema;

/**
 * 高级输入框
 */
public class AdvanceInputField extends SchemaField {
    /**
     * 输入框类型
     * <br>
     * input: 单行 <br>
     * textArea: 多行
     */
    private String textType;
    /**
     * 内容格式
     * <br>
     * 可选值有: text, password, email, tel, id
     */
    private String textContent;
}
