package io.github.airiot.sdk.driver;

import io.github.airiot.sdk.logger.LoggerContexts;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 初始化日志上下文相关信息
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class InitialApplicationRunner implements SpringApplicationRunListener {

    private static String PROJECT_ID;
    private static String SERVICE_ID;
    private static String DRIVER_ID;

    static void loadProperties(String profile) {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        String[] exts = loader.getFileExtensions();
        for (String ext : exts) {
            String filename = "/application." + ext;
            if(StringUtils.hasText(profile)) {
                filename = "/application-" + profile + "." + ext;
            }
            try(InputStream is = InitialApplicationRunner.class.getResourceAsStream(filename)) {
                if(is == null) {
                    continue;
                }
                List<PropertySource<?>> propertySources = loader.load("application", new InputStreamResource(is));

                for (PropertySource<?> propertySource : propertySources) {
                    String driverId = String.valueOf(propertySource.getProperty("airiot.driver.id"));
                    String projectId = String.valueOf(propertySource.getProperty("airiot.driver.project-id"));
                    String serviceId = String.valueOf(propertySource.getProperty("airiot.driver.instance-id"));
                    if(StringUtils.hasText(driverId)) {
                        DRIVER_ID = driverId;
                    }
                    if(StringUtils.hasText(projectId)) {
                        PROJECT_ID = projectId;
                    }
                    if(StringUtils.hasText(serviceId)) {
                        SERVICE_ID = driverId;
                    }
                }
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public InitialApplicationRunner(SpringApplication application, String[] args) {
        loadProperties("");
        if(args != null && args.length > 0) {
            for (String arg : args) {
                String[] kv = arg.split("=");
                if(kv.length != 2) {
                    return;
                }
                String key = kv[0].trim();
                String value = kv[1].trim();

                if(key.startsWith("--")) {
                    key = key.substring(2);
                } else if(key.startsWith("-")) {
                    key = key.substring(1);
                }

                if(key.equals("project") && StringUtils.hasText(value)) {
                    PROJECT_ID = value;
                } else if(key.equals("serviceId") && StringUtils.hasText(value)) {
                    SERVICE_ID = value;
                }
            }

            if(StringUtils.hasText(PROJECT_ID) && StringUtils.hasText(SERVICE_ID)) {
                LoggerContexts.setDefaultProjectId(PROJECT_ID);
            }
        } else {
            if(StringUtils.hasText(PROJECT_ID)) {
                LoggerContexts.setDefaultProjectId(PROJECT_ID);
            }
        }

        if(StringUtils.hasText(PROJECT_ID) && StringUtils.hasText(SERVICE_ID) && StringUtils.hasText(DRIVER_ID)) {
            LoggerContexts.setDefaultService(PROJECT_ID + "-" + SERVICE_ID + "-" + DRIVER_ID);
        }
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        SpringApplicationRunListener.super.environmentPrepared(bootstrapContext, environment);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        String[] profiles = context.getEnvironment().getActiveProfiles();
        for (String profile : profiles) {
            loadProperties(profile);
        }
    }
}
