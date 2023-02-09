package cn.airiot.sdk.client.dubbo.clients.core;

import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dubbo.grpc.api.GetOrDeleteRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import cn.airiot.sdk.client.dubbo.grpc.core.DubboTableSchemaServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.service.core.TableSchemaClient;
import cn.airiot.sdk.client.service.core.dto.table.TableSchema;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class DubboTableSchemaClient implements TableSchemaClient {

    private final Logger logger = LoggerFactory.getLogger(DubboTableSchemaClient.class);
    private final DubboTableSchemaServiceGrpc.ITableSchemaService tableSchemaService;

    public DubboTableSchemaClient(DubboTableSchemaServiceGrpc.ITableSchemaService tableSchemaService) {
        this.tableSchemaService = tableSchemaService;
    }

    @Override
    public Response<List<TableSchema>> query(@Nonnull Query query) {
        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表定义: query = {}", new String(queryData));
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableSchemaService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表定义: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(TableSchema.class, response);
    }

    @Override
    public Response<List<TableSchema>> queryAll() {
        Query query = Query.newBuilder().select(TableSchema.class).build();
        byte[] queryData = query.serialize();
        if (logger.isDebugEnabled()) {
            logger.debug("查询全部工作表定义: query = {}", new String(queryData));
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableSchemaService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询全部工作表定义: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(TableSchema.class, response);
    }

    @Override
    public Response<TableSchema> queryById(@Nonnull String tableId) {
        if (!StringUtils.hasText(tableId)) {
            throw new IllegalArgumentException("'tableId' cannot be null or empty");
        }

        logger.debug("查询工作表定义: tableId = {}", tableId);

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableSchemaService.get(
                GetOrDeleteRequest.newBuilder()
                        .setId(tableId)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询工作表定义: tableId = {}, response = {}", tableId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(TableSchema.class, response);
    }
}
