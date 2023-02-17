package cn.airiot.sdk.client.service.core;


import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.BatchInsertResult;
import cn.airiot.sdk.client.dto.InsertResult;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.service.PlatformClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * 工作表记录操作客户端
 * <br>
 * 用于对工作表中的数据进行增、删、改、查操作
 */
public interface TableDataClient extends PlatformClient {

    /**
     * 向工作表中添加记录
     *
     * @param tableId 工作表标识
     * @param row     记录
     * @param <T>     工作表对应实体类泛型
     */
    <T> Response<InsertResult> create(@Nonnull String tableId, @Nonnull T row);

    /**
     * 向工作表中批量添加记录
     *
     * @param tableId 工作表标识
     * @param rows    记录列表
     * @param <T>     工作表对应实体类泛型
     */
    <T> Response<BatchInsertResult> create(@Nonnull String tableId, @Nonnull List<T> rows);

    /**
     * 更新工作表记录
     *
     * @param tableId 工作表标识
     * @param rowId   记录ID
     * @param data    要更新的记录
     */
    <T> Response<Void> update(@Nonnull String tableId, @Nonnull String rowId, @Nonnull T data);

    /**
     * 批量更新工作表记录.
     * <br>
     * 更新所有与 {@code query} 匹配的记录
     *
     * @param tableId 工作表标识
     * @param query   更新条件
     * @param data    要更新的数据
     */
    <T> Response<Void> update(@Nonnull String tableId, @Nonnull Query query, @Nonnull T data);

    /**
     * 替换记录全部信息
     *
     * @param tableId 工作表标识
     * @param rowId   记录ID
     * @param data    替换后的记录信息
     */
    <T> Response<Void> replace(@Nonnull String tableId, @Nonnull String rowId, @Nonnull T data);

    /**
     * 根据记录ID删除数据
     *
     * @param tableId 工作表标识
     * @param rowId   记录ID
     */
    Response<Void> deleteById(@Nonnull String tableId, @Nonnull String rowId);

    /**
     * 批量删除工作表记录
     *
     * @param tableId 工作表标识
     * @param query   删除条件
     */
    Response<Void> deleteByQuery(@Nonnull String tableId, @Nonnull Query query);

    /**
     * 根据条件查询用户信息
     *
     * @param tClass  工作表记录关联对象类型
     * @param tableId 工作表标识
     * @param query   查询条件
     * @return 用户信息或错误信息
     */
    <T> Response<List<T>> query(@Nonnull Class<T> tClass, @Nonnull String tableId, @Nonnull Query query);

    default Response<List<Map<String, Object>>> query(@Nonnull String tableId, @Nonnull Query query) {
        return this.query(MAP_TYPE, tableId, query);
    }
    
    /**
     * 根据ID查询记录信息
     *
     * @param tClass  工作表记录关联对象类型
     * @param tableId 工作表标识
     * @param rowId   记录ID
     * @return 记录信息或错误信息
     */
    <T> Response<T> queryById(@Nonnull Class<T> tClass, @Nonnull String tableId, @Nonnull  String rowId);
}
