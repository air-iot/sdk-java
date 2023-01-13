package cn.airiot.sdk.driver;

import org.slf4j.MDC;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

public class LoggerApplicationRunListener implements SpringApplicationRunListener {

    public LoggerApplicationRunListener(SpringApplication application, String[] args) {

    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        String projectId = environment.getProperty("airiot.platform.authorization.projectId");
        String driverId = environment.getProperty("airiot.driver.id");
        String driverName = environment.getProperty("airiot.driver.name");
        String driverInstanceId = environment.getProperty("airiot.driver.instanceId");
        
        if (StringUtils.hasText(projectId)) {
            MDC.put("PROJECT_ID", projectId.trim());
        }
        if (StringUtils.hasText(driverId)) {
            MDC.put("DRIVER_ID", driverId.trim());
        }
        if (StringUtils.hasText(driverName)) {
            MDC.put("DRIVER_NAME", driverName.trim());
        }
        if (StringUtils.hasText(driverInstanceId)) {
            MDC.put("DRIVER_INSTANCE", driverInstanceId.trim());
        }
    }
}
