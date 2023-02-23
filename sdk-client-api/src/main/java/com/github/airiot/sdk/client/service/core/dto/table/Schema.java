package com.github.airiot.sdk.client.service.core.dto.table;


import java.util.List;
import java.util.Map;

/**
 * 字段定义
 */
public class Schema {

    private String key;
    private String name;
    private String title;
    private String type;
    private List<String> form;
    private List<String> listFields;
    private Map<String, Map<String, Object>> properties;
    private List<String> required;
}
