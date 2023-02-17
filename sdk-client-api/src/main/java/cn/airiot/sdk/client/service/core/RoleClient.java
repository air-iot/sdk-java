package cn.airiot.sdk.client.service.core;


import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.service.PlatformClient;
import cn.airiot.sdk.client.service.core.dto.Role;

import javax.annotation.Nullable;
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
    Response<List<Role>> query(@Nullable Query query);

    /**
     * 根据角色ID查询角色信息
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Response<Role> queryById(@Nullable String roleId);
}
