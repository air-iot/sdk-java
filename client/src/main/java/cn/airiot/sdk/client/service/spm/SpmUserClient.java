package cn.airiot.sdk.client.service.spm;

import cn.airiot.sdk.client.annotation.DisableAuth;
import cn.airiot.sdk.client.annotation.NonProject;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dto.Token;
import cn.airiot.sdk.client.service.PlatformClient;

import javax.annotation.Nonnull;

/**
 * 空间管理-用户客(租户)户端
 */
@NonProject
public interface SpmUserClient extends PlatformClient {

    /**
     * 使用 {@code appKey} 和 {@code appSecret} 获取租户级 token
     *
     * @param appKey    扩展应用标识
     * @param appSecret 扩展应用密钥
     * @return token
     */
    @DisableAuth
    Response<Token> getToken(@Nonnull String appKey, @Nonnull String appSecret);

}
