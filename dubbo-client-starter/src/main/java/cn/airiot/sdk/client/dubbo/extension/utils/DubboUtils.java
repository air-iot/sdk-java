package cn.airiot.sdk.client.dubbo.extension.utils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.RegistryConstants;
import org.springframework.util.StringUtils;

public class DubboUtils {


    /**
     * 从 {@link URL} 中提取服务名
     *
     * @param url 服务URL
     * @return 服务名
     */
    public static String getServiceName(URL url) {
        String service = url.getParameter(RegistryConstants.PROVIDED_BY, "");

        if (!StringUtils.hasText(service)) {
            service = url.getGroup();
        }

        if (!StringUtils.hasText(service)) {
            throw new IllegalArgumentException("订阅服务: 未定义 provided-by 或 group 信息");
        }

        return service;
    }
}
