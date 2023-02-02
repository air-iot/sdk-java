package cn.airiot.sdk.client.service.core;


import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.service.PlatformClient;
import cn.airiot.sdk.client.service.core.dto.table.TableSchema;

import java.util.List;

/**
 * 工作表定义客户端
 */
public interface TableSchemaClient extends PlatformClient {

    /**
     * 查询工作表定义
     *
     * @return 工作表定义信息
     */
    Response<List<TableSchema>> query(Query query);

    /**
     * 查询全部工作表定义
     *
     * @return 工作表定义信息
     */
    Response<List<TableSchema>> queryAll();

    /**
     * 查询工作表定义
     *
     * @param tableId 表标识
     * @return 工作表定义信息
     */
    Response<TableSchema> queryById(String tableId);
}
