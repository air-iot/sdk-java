package io.github.airiot.sdk.datarelay.application;

import java.util.List;
import java.util.Map;

/**
 * 北向服务应用接口
 *
 * @param <T> 北向服务配置数据类型
 */
public interface DataRelayApp<T> {

    /**
     * 启动服务
     *
     * @param config 服务配置
     */
    void start(T config) throws Exception;

    /**
     * 停止服务.
     * <br>
     * 当服务进程退出时, 会调用该方法. 可以在该方法内执行一些清理动作
     */
    void stop();

    /**
     * HTTP 请求代理.
     *
     * @param requestType 请求类型
     * @param headers     请求头
     * @param requestData 请求数据
     * @return 请求处理结果
     */
    Object proxy(String requestType, Map<String, List<String>> headers, String requestData) throws Exception;
}
