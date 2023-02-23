package com.github.airiot.sdk.client.dubbo.clients.warning;

import com.github.airiot.sdk.client.builder.Query;
import com.github.airiot.sdk.client.dto.Response;
import com.github.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import com.github.airiot.sdk.client.dubbo.grpc.warning.DubboRuleServiceGrpc;
import com.github.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import com.github.airiot.sdk.client.service.warning.RuleClient;
import com.github.airiot.sdk.client.service.warning.dto.Rule;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;


public class DubboRuleClient implements RuleClient {

    private final Logger logger = LoggerFactory.getLogger(DubboRuleClient.class);

    private final DubboRuleServiceGrpc.IRuleService ruleService;

    public DubboRuleClient(DubboRuleServiceGrpc.IRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Override
    public Response<List<Rule>> query(@Nonnull Query query) {
        if (!query.hasSelectFields()) {
            query = query.toBuilder().select(Rule.class).build();
        }

        byte[] queryData = query.serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询报警规则: query = {}", new String(queryData));
        }

        com.github.airiot.sdk.client.dubbo.grpc.api.Response response = this.ruleService.query(
                QueryRequest.newBuilder()
                        .setQuery(ByteString.copyFrom(queryData))
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("查询报警规则: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Rule.class, response);
    }
}
