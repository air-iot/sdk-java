package cn.airiot.sdk.driver.data.model;

public enum FieldType {
    STRING("string"),
    INTEGER("integer"),
    FLOAT("float"),
    BOOLEAN("boolean");

    // 成员变量
    private final String value;

    FieldType(String value) {
        this.value = value;
    }

    public boolean equals(String type) {
        return this.value.equalsIgnoreCase(type);
    }

    public String getValue() {
        return value;
    }
}

