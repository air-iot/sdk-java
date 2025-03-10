package io.github.airiot.sdk.client.http.clients.core;

import feign.Param;
import feign.RequestLine;
import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.http.feign.QueryParamExpander;
import io.github.airiot.sdk.client.service.core.TableRecordClient;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 设备配置客户端
 */
public interface TableRecordFeignClient extends TableRecordClient {

    @RequestLine("GET /core/t/record?query={query}&archive={archive}")
    @Override
    ResponseDTO<List<Map<String, Object>>> query(@Nonnull @Param(value = "query", expander = QueryParamExpander.class) Query query, @Param("archive") boolean archive);

    @RequestLine("GET /core/t/record/{id}")
    ResponseDTO<Map<String, Object>> get(@Param("id") String id);

    @Override
    default ResponseDTO<InsertResult> create(String tableId, String tableDataId, Map<String, Object> record) {
        record.put("tableInfo", Collections.singletonMap("id", tableId));
        record.put("tableData", Collections.singletonMap("id", tableDataId));
        return create(record);
    }

    @RequestLine("POST /core/t/record")
    ResponseDTO<InsertResult> create(Map<String, Object> record);

    @RequestLine("PATCH /core/t/record/{id}")
    @Override
    ResponseDTO<Void> update(@Param("id") String id, Map<String, Object> record);

    @Override
    default ResponseDTO<Void> replace(String id, String tableId, String tableDataId, Map<String, Object> record) {
        record.put("id", id);
        record.put("tableInfo", Collections.singletonMap("id", tableId));
        record.put("tableData", Collections.singletonMap("id", tableDataId));
        ResponseDTO<InsertResult> result = create(record);
        return new ResponseDTO<>(result.isSuccess(), result.getCode(), result.getMessage(), result.getDetail(), null);
    }

    @RequestLine("DELETE /core/t/record/{id}")
    @Override
    ResponseDTO<Void> delete(@Param("id") String id);
}
