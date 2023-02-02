package cn.airiot.sdk.client.dubbo.extension.interceptors;

import cn.airiot.sdk.client.context.RequestContext;
import cn.airiot.sdk.client.exception.NonProjectException;
import cn.airiot.sdk.client.service.AuthorizationClient;
import cn.airiot.sdk.client.service.Constants;
import io.grpc.*;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.config.spring.extension.SpringExtensionInjector;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.apache.dubbo.rpc.protocol.grpc.interceptors.ClientInterceptor;
import org.springframework.util.StringUtils;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;


/**
 * grpc 请求拦截器.
 * <br>
 * 根据上下文信息向请求的元数据中添加 {@code projectId} 和 {@code token} 信息
 */
@Activate(group = CONSUMER)
public class MetadataClientInterceptor implements ClientInterceptor {

    private final AuthorizationClient authorizationClient;

    public MetadataClientInterceptor(FrameworkModel model) {
        this.authorizationClient = SpringExtensionInjector.get(model.defaultApplication())
                .getContext().getBean(AuthorizationClient.class);
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);
        return new MetadataForwardingClientCall<>(method, call);
    }

    class MetadataForwardingClientCall<ReqT, RespT> extends ForwardingClientCall<ReqT, RespT> {

        private final MethodDescriptor<ReqT, RespT> method;
        private final ClientCall<ReqT, RespT> call;

        public MetadataForwardingClientCall(MethodDescriptor<ReqT, RespT> method, ClientCall<ReqT, RespT> call) {
            this.method = method;
            this.call = call;
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            if (RequestContext.isTakeProject()) {
                String projectId = RequestContext.getProjectId();
                if (!StringUtils.hasText(projectId)) {
                    throw new NonProjectException("调用 '" + method.getFullMethodName() + "' 接口失败, 请求上下文中未找到 'projectId' 信息. 请查看 cn.airiot.sdk.client.context.RequestContext");
                }
                headers.put(Metadata.Key.of(Constants.HEADER_PROJECT, Metadata.ASCII_STRING_MARSHALLER), projectId);
            }

            // 如果启用了身份认证
            if (RequestContext.isAuthEnabled()) {
                String token = MetadataClientInterceptor.this.authorizationClient.getToken().getAccessToken();
                headers.put(Metadata.Key.of(Constants.HEADER_AUTHORIZATION, Metadata.ASCII_STRING_MARSHALLER), token);
            }
            super.start(responseListener, headers);
        }

        @Override
        protected ClientCall<ReqT, RespT> delegate() {
            return call;
        }
    }
}
