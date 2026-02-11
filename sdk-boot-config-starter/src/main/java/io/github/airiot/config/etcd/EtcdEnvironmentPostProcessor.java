package io.github.airiot.config.etcd;

import com.google.gson.Gson;
import io.etcd.jetcd.*;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class EtcdEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private final Logger logger = LoggerFactory.getLogger(EtcdEnvironmentPostProcessor.class);

    public static final String AUTHORIZATION_PREFIX = "airiot.client.authorization";

    @Override
    public void postProcessEnvironment(@NonNull ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        // 从 yaml 中读取 etcd 的连接地址
        EtcdConfigProperties etcdConfig = Binder.get(environment)
                .bind(EtcdConfigProperties.PREFIX, EtcdConfigProperties.class)
                .orElse(new EtcdConfigProperties()); // 如果 yaml 里没配，使用默认值

        Boolean liteMode = environment.getProperty("api.litemode", Boolean.class, false);
        if (liteMode) {
            return;
        }

        if (!etcdConfig.isEnabled()) {
            logger.info("平台 ETCD 配置未启用");
            return;
        }

        logger.info("平台 ETCD 配置: {}", etcdConfig);

        // 2. 使用提取到的配置创建客户端
        ClientBuilder builder = Client.builder().endpoints(etcdConfig.getEndpoints().toArray(new String[0]));
        builder.connectTimeout(etcdConfig.getConnectTimeout());
        if (StringUtils.hasText(etcdConfig.getUsername()) && StringUtils.hasText(etcdConfig.getPassword())) {
            builder.user(ByteSequence.from(etcdConfig.getUsername(), StandardCharsets.UTF_8))
                    .password(ByteSequence.from(etcdConfig.getPassword(), StandardCharsets.UTF_8));
        }

        try (Client client = builder.build()) {
            AiriotConfig config = this.getConfig(client, etcdConfig);
            AiriotConfig.API api = config.getApp().getApi();

            Map<String, Object> configs = new HashMap<>();
            if (StringUtils.hasText(api.getType())) {
                configs.put(AUTHORIZATION_PREFIX + ".type", api.getType());
            }
            if (StringUtils.hasText(api.getProjectId())) {
                configs.put(AUTHORIZATION_PREFIX + ".project-id", api.getProjectId());
            }
            if (StringUtils.hasText(api.getAk())) {
                configs.put(AUTHORIZATION_PREFIX + ".app-key", api.getAk());
            }
            if (StringUtils.hasText(api.getSk())) {
                configs.put(AUTHORIZATION_PREFIX + ".app-secret", api.getSk());
            }

            PropertySource<Map<String, Object>> propertySource = new MapPropertySource("airiot-etcd", configs);
            environment.getPropertySources().addFirst(propertySource);
        } catch (Exception e) {
            throw new RuntimeException("读取平台配置异常", e);
        }
    }

    AiriotConfig getConfig(Client client, EtcdConfigProperties etcdConfig) throws Exception {
        try (KV kvClient = client.getKVClient()) {
            List<KeyValue> kvs = kvClient.get(ByteSequence.from(etcdConfig.getConfigKey(), StandardCharsets.UTF_8))
                    .get(etcdConfig.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS)
                    .getKvs();
            if (CollectionUtils.isEmpty(kvs)) {
                logger.warn("平台 ETCD 配置: 未读取到平台配置, {}", etcdConfig.getConfigKey());
                throw new IllegalStateException("未读取到平台配置: " + etcdConfig.getConfigKey());
            }

            String configValue = kvs.getFirst().getValue().toString(StandardCharsets.UTF_8);

            logger.debug("平台 ETCD 配置: {}, {}", etcdConfig.getConfigKey(), configValue);

            AiriotConfig config = new Gson().fromJson(configValue, AiriotConfig.class);
            if (config.getApp() == null || config.getApp().getApi() == null) {
                logger.warn("未找到有效的平台配置: {}, {}", etcdConfig.getConfigKey(), configValue);
                throw new IllegalStateException("未读取到平台配置: " + etcdConfig.getConfigKey());
            }

            logger.debug("平台 ETCD 配置: {}, {}, {}", etcdConfig.getConfigKey(), configValue, config);

            return config;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
