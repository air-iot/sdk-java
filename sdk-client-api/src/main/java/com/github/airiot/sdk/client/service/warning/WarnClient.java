package com.github.airiot.sdk.client.service.warning;


import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.dto.InsertResult;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.service.PlatformClient;
import com.github.airiot.sdk.client.service.warning.dto.Warning;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 告警信息客户端
 */
public interface WarnClient extends PlatformClient {

    /**
     * 查询告警信息
     *
     * @param query   查询条件
     * @param archive
     * @return 告警信息
     */
    Response<List<Warning>> query(@Nonnull Query query, String archive);

    /**
     * 根据告警信息ID查询告警信息
     *
     * @param warningId 告警信息ID
     * @param archive
     * @return 告警信息
     */
    Response<Warning> queryById(@Nonnull String warningId, String archive);

    /**
     * 创建告警
     *
     * @param warning 告警信息
     * @return
     */
    Response<InsertResult> create(@Nonnull Warning warning);
}
