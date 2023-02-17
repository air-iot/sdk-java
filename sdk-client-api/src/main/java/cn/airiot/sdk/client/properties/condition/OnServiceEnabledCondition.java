package cn.airiot.sdk.client.properties.condition;

import cn.airiot.sdk.client.properties.ClientProperties;
import cn.airiot.sdk.client.properties.ServiceType;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;


/**
 * 判断目标服务是否已经启用
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class OnServiceEnabledCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionalOnServiceEnabled.class.getName());
        ServiceType value = (ServiceType) annotationAttributes.get("value");
        String serviceName = value.name().toLowerCase();
        boolean enabled = context.getEnvironment()
                .getProperty(String.format("%s.services.%s.enabled", ClientProperties.PREFIX, serviceName), Boolean.class, false);
        if (enabled) {
            return ConditionOutcome.match(String.format("the service %s is enabled", serviceName));
        }
        return ConditionOutcome.noMatch(String.format("the service %s is disabled", serviceName));
    }
}
