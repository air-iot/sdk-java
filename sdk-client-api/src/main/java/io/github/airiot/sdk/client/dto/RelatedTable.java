package io.github.airiot.sdk.client.dto;


/**
 * 关联表字段定义
 */
public class RelatedTable {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RelatedTable() {}

    public RelatedTable(String id) {
        if(id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
        this.id = id;
    }

    public static RelatedTable of(String id) {
        return new RelatedTable(id);
    }

    @Override
    public String toString() {
        return "RelatedTable{" +
                "id='" + id + '\'' +
                '}';
    }
}
