package cn.airiot.sdk.client.dubbo.clients.core;

import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.service.core.SystemVariableClient;
import cn.airiot.sdk.client.service.core.dto.SystemVariable;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dubbo.grpc.api.GetOrDeleteRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import cn.airiot.sdk.client.dubbo.grpc.core.DubboSystemVariableServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;


@Component
public class DubboSystemVariableClient implements SystemVariableClient {

    private final Logger logger = LoggerFactory.getLogger(DubboSystemVariableClient.class);
    private final DubboSystemVariableServiceGrpc.ISystemVariableService systemVariableService;

    public DubboSystemVariableClient(DubboSystemVariableServiceGrpc.ISystemVariableService systemVariableService) {
        this.systemVariableService = systemVariableService;
    }

    @Override
    public Response<List<SystemVariable>> query(Query query) {
        if (query == null) {
            throw new IllegalArgumentException("'query' cannot be null");
        }

        Query.Builder builder = query.toBuilder();
        if (!builder.containsSelectField(SystemVariable::getValue)) {
            builder.select(SystemVariable::getValue);
        }

        byte[] queryData = builder.build().serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: query = {}", new String(queryData));
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.systemVariableService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(SystemVariable.class, response);
    }

    @Override
    public Response<SystemVariable> queryByUId(String uid) {
        if (!StringUtils.hasText(uid)) {
            throw new IllegalArgumentException("'uid' cannot be null or empty");
        }

        byte[] queryData = Query.newBuilder()
                .select(SystemVariable.class)
                .eq(SystemVariable::getUid, uid)
                .build().serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: uid = {}", uid);
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.systemVariableService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: uid = {}, query = {}, response = {}", uid, new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(SystemVariable.class, response);
    }

    @Override
    public Response<SystemVariable> queryById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("'id' cannot be null or empty");
        }

        logger.debug("查询系统变量: id = {}", id);

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.systemVariableService.get(GetOrDeleteRequest.newBuilder()
                .setId(id)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询系统变量: id = {}, response = {}", id, DubboClientUtils.toString(response));
        }
        
        return DubboClientUtils.deserialize(SystemVariable.class, response);
    }
}
