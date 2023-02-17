package cn.airiot.sdk.client.service.warning;

import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.service.PlatformClient;
import cn.airiot.sdk.client.service.warning.dto.Rule;

import javax.annotation.Nonnull;
import java.util.List;


/**
 * 告警规则客户端
 */
public interface RuleClient extends PlatformClient {

    /**
     * 查询报警规则
     *
     * @param query 查询条件
     * @return 报警规则信息
     */
    Response<List<Rule>> query(@Nonnull Query query);
}
