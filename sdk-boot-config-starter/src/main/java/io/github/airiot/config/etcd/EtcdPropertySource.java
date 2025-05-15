package io.github.airiot.config.etcd;

import com.google.gson.Gson;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EtcdPropertySource extends EnumerablePropertySource<Client> {
    private final Logger logger = LoggerFactory.getLogger(EtcdPropertySource.class);
    public static final String AUTHORIZATION_PREFIX = "airiot.client.authorization";

    private final EtcdConfigProperties properties;

    private final Map<String, Object> configValues = new HashMap<>();

    public EtcdPropertySource(String name, Client source, EtcdConfigProperties properties) {
        super(name, source);
        this.properties = properties;
    }

    protected void init() throws Exception {
        logger.info("从 etc 加载平台配置");
        try (KV kvClient = this.source.getKVClient()) {
            List<KeyValue> kvs = kvClient.get(ByteSequence.from(this.properties.getConfigKey(), StandardCharsets.UTF_8))
                    .get(this.properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS)
                    .getKvs();
            if (CollectionUtils.isEmpty(kvs)) {
                logger.warn("未请求到平台配置: {}", this.properties.getConfigKey());
                return;
            }

            String configValue = kvs.get(0).getValue().toString(StandardCharsets.UTF_8);

            logger.debug("请求到平台配置: {}, {}", this.properties.getConfigKey(), configValue);

            AiriotConfig config = new Gson().fromJson(configValue, AiriotConfig.class);
            if (config.getApp() == null || config.getApp().getApi() == null) {
                logger.warn("未找到有效的平台配置: {}, {}", this.properties.getConfigKey(), configValue);
                return;
            }

            logger.debug("平台配置: {}, {}, {}", this.properties.getConfigKey(), configValue, config);

            AiriotConfig.API api = config.getApp().getApi();

            if (StringUtils.hasText(api.getType())) {
                this.configValues.put(AUTHORIZATION_PREFIX + ".type", api.getType());
            }
            if (StringUtils.hasText(api.getProjectId())) {
                this.configValues.put(AUTHORIZATION_PREFIX + ".project-id", api.getProjectId());
            }
            if (StringUtils.hasText(api.getAk())) {
                this.configValues.put(AUTHORIZATION_PREFIX + ".app-key", api.getAk());
            }
            if (StringUtils.hasText(api.getSk())) {
                this.configValues.put(AUTHORIZATION_PREFIX + ".app-secret", api.getSk());
            }
        }
    }

    @Override
    public String[] getPropertyNames() {
        return this.configValues.keySet().toArray(new String[0]);
    }

    @Override
    public Object getProperty(String name) {
        return this.configValues.get(name);
    }
}
