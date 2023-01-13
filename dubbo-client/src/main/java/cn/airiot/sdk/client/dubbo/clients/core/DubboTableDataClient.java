package cn.airiot.sdk.client.dubbo.clients.core;

import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.core.TableDataClient;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dubbo.grpc.core.CreateDataRequest;
import cn.airiot.sdk.client.dubbo.grpc.core.DubboTableDataServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;


@Component
public class DubboTableDataClient implements TableDataClient {

    private final Logger logger = LoggerFactory.getLogger("Dubbo-TableData-Client");

    private final DubboTableDataServiceGrpc.ITableDataService tableDataService;

    public DubboTableDataClient(DubboTableDataServiceGrpc.ITableDataService tableDataService) {
        this.tableDataService = tableDataService;
    }

    @Override
    public <T> Response<Void> create(String tableId, T row) {
        if (!StringUtils.hasText(tableId)) {
            throw new IllegalArgumentException("tableId cannot be null or empty");
        }

        if (row == null) {
            throw new IllegalArgumentException("the row data cannot be null");
        }

        ByteString rowData = DubboClientUtils.serialize(row);

        if (logger.isDebugEnabled()) {
            logger.debug("添加工作表记录: tableId = {}, rowData = {}", tableId, rowData.toStringUtf8());
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.tableDataService.create(CreateDataRequest.newBuilder()
                .setTable(tableId.trim())
                .setData(rowData)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("添加工作表记录: tableId = {}, rowData = {}, response = {}",
                    tableId, rowData.toStringUtf8(), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public <T> Response<Void> create(String tableId, List<T> rows) {
        return null;
    }

    @Override
    public <T> Response<Void> update(String tableId, String rowId, T data) {
        return null;
    }

    @Override
    public <T> Response<Void> update(String tableId, Query query, T data) {
        return null;
    }

    @Override
    public <T> Response<Void> replace(String tableId, String rowId, T data) {
        return null;
    }

    @Override
    public Response<Void> deleteById(String tableId, String rowId) {
        return null;
    }

    @Override
    public Response<Void> deleteByQuery(String tableId, Query query) {
        return null;
    }

    @Override
    public <T> Response<List<T>> query(Class<T> tClass, String tableId, Query query) {
        return null;
    }

    @Override
    public <T> Response<T> getById(Class<T> tClass, String tableId, String rowId) {
        return null;
    }
}
