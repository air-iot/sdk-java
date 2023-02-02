package cn.airiot.sdk.driver.data.model;


/**
 * 数据点. 该类中的字段为公共属性, 如果在 {@code schema.js} 中扩展了 tag 信息, 需要自己实现编写子类并添加扩展的字段信息
 */
public class Tag {
    /**
     * 数据点标识
     */
    private String id;
    /**
     * 数据点名称
     */
    private String name;
    /**
     *
     */
    private TagValue tagValue;
    /**
     * 数据点有效范围值
     */
    private Range range;
    /**
     *
     */
    private Integer fixed;
    private Float mod;

    public Tag() {
    }

    public Tag(String id, String name, TagValue tagValue, Range range, Integer fixed, Float mod) {
        this.id = id;
        this.name = name;
        this.tagValue = tagValue;
        this.range = range;
        this.fixed = fixed;
        this.mod = mod;
    }

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

    public TagValue getTagValue() {
        return tagValue;
    }

    public void setTagValue(TagValue tagValue) {
        this.tagValue = tagValue;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Integer getFixed() {
        return fixed;
    }

    public void setFixed(Integer fixed) {
        this.fixed = fixed;
    }

    public Float getMod() {
        return mod;
    }

    public void setMod(Float mod) {
        this.mod = mod;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tagValue=" + tagValue +
                ", range=" + range +
                ", fixed=" + fixed +
                ", mod=" + mod +
                '}';
    }
}
