package com.github.airiot.sdk.client.service.core;


import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.service.PlatformClient;
import com.github.airiot.sdk.client.service.core.dto.table.TableSchema;

import javax.annotation.Nonnull;
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
    Response<List<TableSchema>> query(@Nonnull Query query);

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
    Response<TableSchema> queryById(@Nonnull String tableId);
}
