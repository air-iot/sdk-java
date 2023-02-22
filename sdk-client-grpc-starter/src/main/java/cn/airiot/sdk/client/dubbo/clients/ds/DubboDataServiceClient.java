package cn.airiot.sdk.client.dubbo.clients.ds;

import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dubbo.grpc.datasource.DubboDataServiceGrpc;
import cn.airiot.sdk.client.dubbo.grpc.datasource.Request;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.service.ds.DataServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class DubboDataServiceClient implements DataServiceClient {

    private final Logger logger = LoggerFactory.getLogger(DubboDataServiceClient.class);

    private final DubboDataServiceGrpc.IDataService dataService;

    public DubboDataServiceClient(DubboDataServiceGrpc.IDataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public <T> Response<T> call(@Nonnull Class<T> tClass, @Nonnull String dsId, @Nullable Map<String, Object> params) {
        if (!StringUtils.hasText(dsId)) {
            throw new IllegalArgumentException("'dsId' cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("调用数据接口: dsId = {}, params = {}, returnType = {}", dsId, params, tClass.getName());
        }

        Request.Builder builder = Request.newBuilder()
                .setKey(dsId);

        if (!CollectionUtils.isEmpty(params)) {
            builder.setData(
                    DubboClientUtils.serialize(params)
            );
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.dataService.proxy(builder.build());

        if (logger.isDebugEnabled()) {
            logger.debug("调用数据接口: dsId = {}, params = {}, response = {}", dsId, params, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(tClass, response);
    }
}
