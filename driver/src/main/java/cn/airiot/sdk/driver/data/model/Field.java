package cn.airiot.sdk.driver.data.model;


import cn.airiot.sdk.driver.model.Tag;

public class Field {
    
    private Tag tag;
    private Object value;

    public Field() {

    }

    public Field(Tag tag, Object value) {
        this.tag = tag;
        this.value = value;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Field{" +
                "tag=" + tag +
                ", value=" + value +
                '}';
    }
}
