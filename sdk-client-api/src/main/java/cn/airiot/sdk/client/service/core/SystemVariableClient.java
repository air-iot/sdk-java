package cn.airiot.sdk.client.service.core;


import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.service.PlatformClient;
import cn.airiot.sdk.client.service.core.dto.SystemVariable;
import cn.airiot.sdk.client.dto.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 系统变量(数据字典)客户端
 */
public interface SystemVariableClient extends PlatformClient {

    /**
     * 查询系统变量信息
     *
     * @param query 查询条件
     * @return 系统变量信息
     */
    Response<List<SystemVariable>> query(@Nonnull Query query);

    /**
     * 根据系统变量编号查询系统变量信息
     *
     * @param uid 系统变量编号
     * @return 系统变量信息
     */
    Response<SystemVariable> queryByUId(@Nonnull String uid);

    /**
     * 根据系统变量ID查询系统变量信息
     *
     * @param id 系统变量ID
     * @return 系统变量信息
     */
    Response<SystemVariable> queryById(@Nonnull String id);

}
