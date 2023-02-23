package com.github.airiot.sdk.client.service.core;


import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.service.PlatformClient;
import com.github.airiot.sdk.client.service.core.dto.Role;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 角色客户端
 */
public interface RoleClient extends PlatformClient {

    /**
     * 查询角色信息
     *
     * @param query 查询条件
     * @return 角色信息
     */
    Response<List<Role>> query(@Nonnull Query query);

    /**
     * 根据角色ID查询角色信息
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Response<Role> queryById(@Nonnull String roleId);
}
