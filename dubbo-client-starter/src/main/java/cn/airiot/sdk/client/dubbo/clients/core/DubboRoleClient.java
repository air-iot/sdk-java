package cn.airiot.sdk.client.dubbo.clients.core;

import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.service.core.RoleClient;
import cn.airiot.sdk.client.service.core.dto.Role;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dubbo.grpc.api.GetOrDeleteRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import cn.airiot.sdk.client.dubbo.grpc.core.DubboRoleServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;


@Component
public class DubboRoleClient implements RoleClient {

    private final Logger logger = LoggerFactory.getLogger(DubboRoleClient.class);

    private final DubboRoleServiceGrpc.IRoleService roleService;

    public DubboRoleClient(DubboRoleServiceGrpc.IRoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public Response<List<Role>> query(Query query) {
        if (query == null) {
            throw new IllegalArgumentException("the 'query' cannot be null");
        }

        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询角色信息: query = {}", new String(queryData));
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.roleService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询角色信息: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Role.class, response);
    }

    @Override
    public Response<Role> queryById(String roleId) {
        if (!StringUtils.hasText(roleId)) {
            throw new IllegalArgumentException("the 'roleId' cannot be null or empty");
        }

        logger.debug("查询角色信息: roleId = {}", roleId);

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.roleService.get(
                GetOrDeleteRequest.newBuilder().setId(roleId).build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询角色信息: roleId = {}, response = {}", roleId, DubboClientUtils.toString(response));
        }
        
        return DubboClientUtils.deserialize(Role.class, response);
    }
}
