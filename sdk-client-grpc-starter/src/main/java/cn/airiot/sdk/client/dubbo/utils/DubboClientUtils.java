package cn.airiot.sdk.client.dubbo.utils;

import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.properties.ServiceConfig;
import cn.airiot.sdk.client.properties.ServiceType;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import org.apache.dubbo.common.constants.LoadbalanceRules;
import org.apache.dubbo.config.spring.ReferenceBean;
import org.apache.dubbo.config.spring.reference.ReferenceBeanBuilder;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class DubboClientUtils {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss+HH:mm")
            .create();
    private static final Gson GSON_WITHOUT_ID = new GsonBuilder()
            .addSerializationExclusionStrategy(new ExclusionByFieldNameStrategy("id"))
            .create();

    public static <T> ReferenceBean<T> createDubboReference(ServiceType serviceType, ServiceConfig config, Class<T> tClass) {
        return new ReferenceBeanBuilder()
                .setCheck(false)
                .setProvidedBy(serviceType.getName())
                .setScope(org.apache.dubbo.rpc.Constants.SCOPE_REMOTE)
                .setLoadBalance(LoadbalanceRules.ROUND_ROBIN)
                .setInterface(tClass)
                .setRetries(config.getRetries())
                .setTimeout((int) config.getTimeout().toMillis())
                .build();
    }

    public static <T extends GeneratedMessageV3> String toString(T message) {
        return message.toByteString().toString(StandardCharsets.UTF_8);
    }

    /**
     * 序列化对象, 并且在序列化过程中会跳过 {@code id} 字段
     *
     * @param data 被序列化的对象
     * @param <T>  被序列化对象的类型
     * @return protobuf 格式字符串
     */
    public static <T> ByteString serializeWithoutId(T data) {
        String str = GSON_WITHOUT_ID.toJson(data);
        return ByteString.copyFrom(str, StandardCharsets.UTF_8);
    }

    /**
     * 序列化对象
     *
     * @param data 被序列化的对象
     * @param <T>  被序列化对象的类型
     * @return protobuf 格式字符串
     */
    public static <T> ByteString serialize(T data) {
        String str = GSON.toJson(data);
        return ByteString.copyFrom(str, StandardCharsets.UTF_8);
    }

    public static <T> Response<List<T>> deserializeList(Class<T> returnType, cn.airiot.sdk.client.dubbo.grpc.api.Response response) {
        List<T> result = null;
        if (response.getStatus()) {
            if (response.getResult().isEmpty()) {
                throw new IllegalStateException("处理响应结果: 请求成功, 但 result 为空");
            }

            try {
                result = (List<T>) GSON.fromJson(response.getResult().toString(StandardCharsets.UTF_8), TypeToken.getParameterized(List.class, returnType));
            } catch (JsonSyntaxException e) {
                throw new IllegalStateException("处理响应结果: 请求成功, 但解析 result 失败, result = " + response.getResult().toString(StandardCharsets.UTF_8), e);
            }

            if (result == null) {
                throw new IllegalStateException("处理响应结果: 请求成功, 但解析 result 失败");
            }
        }

        return new Response<>(response.getStatus(), response.getCount(), response.getCode(), response.getInfo(), response.getDetail(), result);
    }

    public static <T> Response<T> deserialize(Class<T> returnType, cn.airiot.sdk.client.dubbo.grpc.api.Response response) {
        T result = null;
        if (response.getStatus()) {
            if (response.getResult().isEmpty()) {
                throw new IllegalStateException("处理新增记录响应结果: 请求成功, 但 result 为空");
            }

            try {
                result = GSON.fromJson(response.getResult().toString(StandardCharsets.UTF_8), returnType);
            } catch (JsonSyntaxException e) {
                throw new IllegalStateException("处理新增记录响应结果: 请求成功, 但解析 result 失败, result = " + response.getResult().toString(StandardCharsets.UTF_8), e);
            }

            if (result == null) {
                throw new IllegalStateException("处理新增记录响应结果: 请求成功, 但解析 result 失败");
            }
        }

        return new Response<>(response.getStatus(), response.getCount(), response.getCode(), response.getInfo(), response.getDetail(), result);
    }

    public static <T> String format(Response<T> response) {
        return GSON.toJson(response);
    }

    /**
     * 序列化时跳过指定名称的字段
     */
    static class ExclusionByFieldNameStrategy implements ExclusionStrategy {

        private final Set<String> excludeFieldNames;

        public ExclusionByFieldNameStrategy(Collection<String> fieldNames) {
            excludeFieldNames = new HashSet<>(fieldNames);
        }

        public ExclusionByFieldNameStrategy(String fieldName) {
            this(Collections.singleton(fieldName));
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return this.excludeFieldNames.contains(f.getName());
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }
}
