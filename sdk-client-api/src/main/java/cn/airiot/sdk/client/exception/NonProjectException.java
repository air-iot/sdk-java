package cn.airiot.sdk.client.exception;


import cn.airiot.sdk.client.context.RequestContext;

/**
 * 请求上下文中未找到 {@code projectId} 信息时抛出的异常.
 * <br>
 * 如果 {@link RequestContext#getProjectId()} 返回的信息为 {@code null} 或空字符串并且客户实现类或方法上没有 {@link cn.airiot.sdk.client.annotation.NonProject} 注解时则抛出该异常
 */
public class NonProjectException extends PlatformException {
    public NonProjectException(String message) {
        super(message);
    }
}
