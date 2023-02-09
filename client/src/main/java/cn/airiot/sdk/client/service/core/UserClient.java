package cn.airiot.sdk.client.service.core;


import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.service.PlatformClient;
import cn.airiot.sdk.client.service.core.dto.User;
import cn.airiot.sdk.client.dto.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 用户服务客户端
 */
public interface UserClient extends PlatformClient {

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 用户ID或错误信息
     */
    Response<User> create(@Nonnull User user);

    /**
     * 更新用户信息
     *
     * @param user 要更新的用户信息
     * @return 更新结果
     */
    Response<Void> update(@Nonnull User user);

    /**
     * 替换用户全部信息
     *
     * @param user 替换后的用户信息
     * @return 替换结果
     */
    Response<Void> replace(@Nonnull User user);

    /**
     * 根据用户ID删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    Response<Void> deleteById(@Nonnull String userId);

    /**
     * 根据条件查询用户信息
     *
     * @param query 查询条件
     * @return 用户信息或错误信息
     */
    Response<List<User>> query(@Nonnull Query query);

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息或错误信息
     */
    Response<User> getById(@Nonnull String userId);
}
