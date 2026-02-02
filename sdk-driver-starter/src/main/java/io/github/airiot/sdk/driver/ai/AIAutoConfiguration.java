package io.github.airiot.sdk.driver.ai;


import io.github.airiot.sdk.driver.DriverApp;
import io.github.airiot.sdk.driver.data.DataDispatchers;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AIServerProperties.class)
@ConditionalOnExpression("${airiot.driver.ai.enabled:false}")
public class AIAutoConfiguration {

    @Bean
    public AIServer aiServer(AIServerProperties properties, ObjectProvider<DriverApp> driverApp, DataDispatchers dispatchers) {
        DriverApp<Object, Object, Object> app = driverApp.getIfUnique();
        if (app == null) {
            throw new BeanCreationException("未找到或找到多个 DriverApp 实例");
        }
        return new AIServer(properties, app, dispatchers);
    }
}
