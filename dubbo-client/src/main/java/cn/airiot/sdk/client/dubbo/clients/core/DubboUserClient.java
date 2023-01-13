package cn.airiot.sdk.client.dubbo.clients.core;

import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.core.UserClient;
import cn.airiot.sdk.client.core.dto.User;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dubbo.grpc.api.CreateRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.GetOrDeleteRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.UpdateRequest;
import cn.airiot.sdk.client.dubbo.grpc.core.DubboUserServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;


@Component
public class DubboUserClient implements UserClient {

    private final Logger logger = LoggerFactory.getLogger("Dubbo-User-Client");

    private final DubboUserServiceGrpc.IUserService userService;

    public DubboUserClient(DubboUserServiceGrpc.IUserService userService) {
        this.userService = userService;
    }

    @Override
    public Response<User> create(User user) {
        logger.debug("创建用户: {}", user);
        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.userService.create(CreateRequest.newBuilder()
                .setData(DubboClientUtils.serialize(user))
                .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("创建用户: user = {}, response = {}", user, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(User.class, response);
    }

    @Override
    public Response<Void> update(User user) {
        if (logger.isDebugEnabled()) {
            logger.debug("更新用户信息: {}", user);
        }

        String userId = user.getId();
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("the id of user cannot be empty");
        }

        ByteString data = DubboClientUtils.serializeWithoutId(user);
        if ("{}".equals(data.toString(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("The update content cannot be empty");
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.userService.update(
                UpdateRequest.newBuilder()
                        .setId(userId.trim())
                        .setData(DubboClientUtils.serializeWithoutId(user))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("更新用户信息: userId = {}, user = {}, update = {}, response = {}",
                    userId, user, data.toStringUtf8(), DubboClientUtils.serialize(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public Response<Void> replace(User user) {
        if (logger.isDebugEnabled()) {
            logger.debug("替换用户信息: {}", user);
        }

        String userId = user.getId();
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("the id of user cannot be empty");
        }

        ByteString data = DubboClientUtils.serializeWithoutId(user);
        if ("{}".equals(data.toString(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("The replace content cannot be empty");
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.userService.update(
                UpdateRequest.newBuilder()
                        .setId(userId.trim())
                        .setData(DubboClientUtils.serializeWithoutId(user))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("替换用户信息: userId = {}, user = {}, update = {}, response = {}",
                    userId, user, data.toStringUtf8(), DubboClientUtils.serialize(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public Response<Void> deleteById(String userId) {
        logger.debug("删除用户: {}", userId);
        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.userService.delete(
                GetOrDeleteRequest.newBuilder().setId(userId).build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("删除用户: userId = {}, response = {}", userId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public Response<List<User>> query(Query query) {
        byte[] queryData = query.toBytes();
        if (logger.isDebugEnabled()) {
            logger.debug("查询用户: {}", new String(queryData, StandardCharsets.UTF_8));
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.userService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询用户: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(User.class, response);
    }

    @Override
    public Response<User> getById(String userId) {
        logger.debug("查询用户: userId = {}", userId);
        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.userService.get(GetOrDeleteRequest.newBuilder()
                .setId(userId)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询用户: userId = {}, response = {}", userId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(User.class, response);
    }
}
