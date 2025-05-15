package io.github.airiot.config.lite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class LitePropertySource extends EnumerablePropertySource<LiteAPIConfig> {
    private final Logger logger = LoggerFactory.getLogger(LitePropertySource.class);
    public static final String HTTP_CLIENT_PREFIX = "airiot.client.http";
    public static final String AUTHORIZATION_PREFIX = "airiot.client.authorization";

    private final Map<String, Object> configValues = new HashMap<>();

    public LitePropertySource(String name, LiteAPIConfig config) {
        super(name, config);
    }

    protected void init() {
        logger.info("获取到 Lite 版本平台配置");

        logger.debug("从环境变量读取到配置: {}", this.source);

        if(StringUtils.hasText(this.source.getGateway())) {
            this.configValues.put(HTTP_CLIENT_PREFIX + ".host", this.source.getGateway());
        }

        if (StringUtils.hasText(this.source.getType())) {
            this.configValues.put(AUTHORIZATION_PREFIX + ".type", this.source.getType());
        }
        if (StringUtils.hasText(this.source.getProjectId())) {
            this.configValues.put(AUTHORIZATION_PREFIX + ".project-id", this.source.getProjectId());
        }
        if (StringUtils.hasText(this.source.getProjectId())) {
            this.configValues.put(AUTHORIZATION_PREFIX + ".project-id", this.source.getProjectId());
        }
        if (StringUtils.hasText(this.source.getAk())) {
            this.configValues.put(AUTHORIZATION_PREFIX + ".app-key", this.source.getAk());
        }
        if (StringUtils.hasText(this.source.getSk())) {
            this.configValues.put(AUTHORIZATION_PREFIX + ".app-secret", this.source.getSk());
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
