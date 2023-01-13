package cn.airiot.sdk.client.dubbo.extension.interceptors;

import io.grpc.*;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.protocol.grpc.interceptors.ClientInterceptor;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

@Activate(group = CONSUMER)
public class MetadataClientInterceptor implements ClientInterceptor {

    private final Metadata metadata = new Metadata();

    public MetadataClientInterceptor() {
        metadata.put(
                Metadata.Key.of("x-request-project", Metadata.ASCII_STRING_MARSHALLER),
                "625f6dbf5433487131f09ff7"
        );
        metadata.put(
                Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER),
                "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NzQ0NTYzOTUsImlhdCI6MTY3MzI0Njc5NSwibmJmIjoxNjczMjQ2Nzk1LCJzdWIiOiJhZG1pbiIsInByb2plY3RJZCI6IjYyNWY2ZGJmNTQzMzQ4NzEzMWYwOWZmNyJ9.Ev-gLXq-5HyadtJNeLpIvQDyCCoUO34jwZhcdJrFk7-2jZf0NxJlxnrlHxFjTH3fLi8wWVWVak9vNdbXz-HcuA"
        );
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

        return new ForwardingClientCall<ReqT, RespT>() {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.merge(metadata);
                super.start(responseListener, headers);
            }

            @Override
            protected ClientCall<ReqT, RespT> delegate() {
                return call;
            }
        };
    }
}
