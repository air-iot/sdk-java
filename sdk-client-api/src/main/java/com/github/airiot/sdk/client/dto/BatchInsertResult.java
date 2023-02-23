package com.github.airiot.sdk.client.dto;


import java.util.List;

/**
 * 新增记灵返回结果
 */
public class BatchInsertResult {

    /**
     * 新增记录ID
     */
    private List<String> InsertedIDs;
    
    public List<String> getInsertedIDs() {
        return InsertedIDs;
    }

    @Override
    public String toString() {
        return "BatchInsertResult{" +
                "InsertedIDs=" + InsertedIDs +
                ", insertedIDs=" + getInsertedIDs() +
                '}';
    }
}
