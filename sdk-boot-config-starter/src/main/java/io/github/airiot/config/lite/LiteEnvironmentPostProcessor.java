package io.github.airiot.config.lite;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class LiteEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private final Logger logger = LoggerFactory.getLogger(LiteEnvironmentPostProcessor.class);

    public static final String HTTP_CLIENT_PREFIX = "airiot.client.http";
    public static final String AUTHORIZATION_PREFIX = "airiot.client.authorization";

    @Override
    public void postProcessEnvironment(@NonNull ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        // 从 yaml 中读取 etcd 的连接地址
        LiteAPIConfig liteConfig = Binder.get(environment)
                .bind(LiteAPIConfig.PREFIX, LiteAPIConfig.class)
                .orElse(new LiteAPIConfig()); // 如果 yaml 里没配，使用默认值

        if (!Boolean.TRUE.equals(liteConfig.getLiteMode())) {
            logger.info("当前不是 Lite 模式");
            return;
        }

        Map<String, Object> configs = new HashMap<>();
        logger.info("平台 Lite 配置: {}", liteConfig);

        if (StringUtils.hasText(liteConfig.getGateway())) {
            configs.put(HTTP_CLIENT_PREFIX + ".host", liteConfig.getGateway());
        }

        if (StringUtils.hasText(liteConfig.getType())) {
            configs.put(AUTHORIZATION_PREFIX + ".type", liteConfig.getType());
        }
        if (StringUtils.hasText(liteConfig.getProjectId())) {
            configs.put(AUTHORIZATION_PREFIX + ".project-id", liteConfig.getProjectId());
        }
        if (StringUtils.hasText(liteConfig.getProjectId())) {
            configs.put(AUTHORIZATION_PREFIX + ".project-id", liteConfig.getProjectId());
        }
        if (StringUtils.hasText(liteConfig.getAk())) {
            configs.put(AUTHORIZATION_PREFIX + ".app-key", liteConfig.getAk());
        }
        if (StringUtils.hasText(liteConfig.getSk())) {
            configs.put(AUTHORIZATION_PREFIX + ".app-secret", liteConfig.getSk());
        }

        PropertySource<Map<String, Object>> propertySource = new MapPropertySource("airiot-lite", configs);
        environment.getPropertySources().addFirst(propertySource);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
