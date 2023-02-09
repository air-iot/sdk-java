package cn.airiot.sdk.client.service.ds;


import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.service.PlatformClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * 数据接口客户端, 用于调用平台已创建的数据接口
 */
public interface DataServiceClient extends PlatformClient {

    /**
     * 调用数据接口
     *
     * @param tClass 接口返回值类型
     * @param dsId   接口标识
     * @param params 参数列表, 即数据接口中添加的参数, 如果没有定义参数则传 {@code null}. <br> key: 参数名. <br> value: 参数值.
     * @return 请求结果
     */
    <T> Response<T> call(@Nonnull Class<T> tClass, @Nonnull String dsId, @Nullable Map<String, Object> params);

}
