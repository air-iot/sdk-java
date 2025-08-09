package io.github.airiot.sdk.client.service.core;

import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;

import java.util.List;
import java.util.Map;

/**
 * 表配置管理接口
 */
public interface TableRecordClient extends PlatformClient {

    /**
     * 查设备配置信息
     *
     * @param query     查询信息
     * @param archive   是否是操作归档表的请求
     * @param queryRule 是否查询报警规则
     */
    ResponseDTO<List<Map<String, Object>>> query(Query query, boolean archive, boolean queryRule);

    default ResponseDTO<List<Map<String, Object>>> query(Query query, boolean archive) {
        return query(query, archive, true);
    }

    /**
     * 获取设备配置信息
     *
     * @param id        资产配置ID
     * @param queryRule 是否查询报警规则
     * @return 返回设备配置信息
     */
    ResponseDTO<Map<String, Object>> get(String id, boolean queryRule);

    default ResponseDTO<Map<String, Object>> get(String id) {
        return get(id, true);
    }

    /**
     * 创建设备配置信息
     * <pre>
     *     示例:
     *
     *     {
     *         "gis": {
     *             "lonlatType": "normal",
     *             "location": { ... }
     *         },
     *         ...
     *     }
     * </pre>
     *
     * @param tableId     表标识
     * @param tableDataId 设备编号
     * @param record      记录信息
     * @return 返回新增设备配置记录ID
     */
    ResponseDTO<InsertResult> create(String tableId, String tableDataId, Map<String, Object> record);

    /**
     * 更新设备配置信息. 该接口只会替换传入的字段, 不会删除其他字段
     *
     * @param id     设备配置记录ID. {@link #create} 创建接口返回的记录ID
     * @param record 需要更新的内容
     */
    ResponseDTO<Void> update(String id, Map<String, Object> record);

    /**
     * 替换设备配置信息. 该方法会直接替换整个设备配置信息
     *
     * @param id     设备配置记录ID. {@link #create} 创建接口返回的记录ID
     * @param record 替换后的设备配置信息
     */
    ResponseDTO<Void> replace(String id, String tableId, String tableDataId, Map<String, Object> record);

    /**
     * 删除设备配置信息
     *
     * @param id 设备配置记录ID. {@link #create} 创建接口返回的记录ID
     */
    ResponseDTO<Void> delete(String id);
}
