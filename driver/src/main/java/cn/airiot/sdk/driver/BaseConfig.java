package cn.airiot.sdk.driver;

import cn.airiot.sdk.driver.data.model.Tag;

import java.util.List;


/**
 * 驱动基础配置
 */
public class BaseConfig {

    /**
     * 数据点列表
     */
    private List<Tag> tags;

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
