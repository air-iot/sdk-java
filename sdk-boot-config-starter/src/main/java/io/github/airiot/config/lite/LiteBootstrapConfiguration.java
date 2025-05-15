package io.github.airiot.config.lite;


import io.github.airiot.config.ModelConditionOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lite 模式配置
 */
@Configuration
@EnableConfigurationProperties(LiteAPIConfig.class)
@ModelConditionOnProperty(value = ModelConditionOnProperty.LITE)
public class LiteBootstrapConfiguration {

    @Bean
    public LitePropertySourceLocator litePropertiesSource(LiteAPIConfig config) {
        return new LitePropertySourceLocator(config);
    }

}
