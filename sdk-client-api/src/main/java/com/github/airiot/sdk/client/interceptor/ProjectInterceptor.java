package com.github.airiot.sdk.client.interceptor;

import com.github.airiot.sdk.client.annotation.NonProject;
import com.github.airiot.sdk.client.context.RequestContext;
import com.github.airiot.sdk.client.service.PlatformClient;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * 拦截所有 {@link PlatformClient} 接口的实现类, 如果实现类或调用的接口上添加了 {@link NonProject} 注解时则不传递项目信息
 */
public class ProjectInterceptor extends AbstractClientInterceptor {

    public ProjectInterceptor() {
        super(NonProject.class, PlatformClient.class);
    }

    @Override
    public Advice getAdvice() {
        return new MethodInterceptor() {
            @Nullable
            @Override
            public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
                RequestContext.disableTakeProject();
                try {
                    return invocation.proceed();
                } finally {
                    RequestContext.enableTakeProject();
                }
            }
        };
    }
}
