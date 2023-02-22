package cn.airiot.sdk.client.dubbo.clients.warning;

import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.InsertResult;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dubbo.grpc.api.CreateRequest;
import cn.airiot.sdk.client.dubbo.grpc.warning.DubboWarnServiceGrpc;
import cn.airiot.sdk.client.dubbo.grpc.warning.GetOrDeleteWarningRequest;
import cn.airiot.sdk.client.dubbo.grpc.warning.QueryWarningRequest;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.service.warning.WarnClient;
import cn.airiot.sdk.client.service.warning.dto.Warning;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;


public class DubboWarnClient implements WarnClient {

    private final Logger logger = LoggerFactory.getLogger(DubboWarnClient.class);

    private final DubboWarnServiceGrpc.IWarnService warnService;

    public DubboWarnClient(DubboWarnServiceGrpc.IWarnService warnService) {
        this.warnService = warnService;
    }

    @Override
    public Response<List<Warning>> query(@Nonnull Query query, String archive) {
        if (!query.hasSelectFields()) {
            query = query.toBuilder().select(Warning.class).build();
        }

        byte[] queryData = query.serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询告警信息: query = {}, archive = {}", new String(queryData), archive);
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.warnService.query(
                QueryWarningRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .setArchive(archive)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询告警信息: query = {}, archive = {}, response = {}", new String(queryData), archive, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Warning.class, response);
    }

    @Override
    public Response<Warning> queryById(@Nonnull String warningId, String archive) {
        if (!StringUtils.hasText(warningId)) {
            throw new IllegalArgumentException("'warningId' cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("查询告警信息: warningId = {}, archive = {}", warningId, archive);
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.warnService.get(
                GetOrDeleteWarningRequest.newBuilder()
                        .setId(warningId)
                        .setArchive(archive)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询告警信息: warningId = {}, archive = {}, response = {}", warningId, archive, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Warning.class, response);
    }

    @Override
    public Response<InsertResult> create(@Nonnull Warning warning) {
        logger.debug("创建告警: {}", warning);

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.warnService.create(CreateRequest.newBuilder()
                .setData(DubboClientUtils.serialize(warning))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("创建告警: warning = {}, response = {}", warning, response);
        }

        return DubboClientUtils.deserialize(InsertResult.class, response);
    }
}
