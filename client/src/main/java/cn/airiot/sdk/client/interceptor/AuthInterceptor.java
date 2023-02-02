package cn.airiot.sdk.client.interceptor;

import cn.airiot.sdk.client.annotation.DisableAuth;
import cn.airiot.sdk.client.context.RequestContext;
import cn.airiot.sdk.client.service.PlatformClient;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthInterceptor extends AbstractClientInterceptor {

    public AuthInterceptor() {
        super(DisableAuth.class, PlatformClient.class);
    }
    
    @Override
    public Advice getAdvice() {
        return new MethodInterceptor() {
            @Nullable
            @Override
            public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
                RequestContext.disableAuth();
                try {
                    return invocation.proceed();
                } finally {
                    RequestContext.enableAuth();
                }
            }
        };
    }
}
