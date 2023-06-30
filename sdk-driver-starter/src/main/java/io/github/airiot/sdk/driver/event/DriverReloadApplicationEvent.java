package io.github.airiot.sdk.driver.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * 驱动重载事件
 */
public class DriverReloadApplicationEvent extends ApplicationEvent {

    public DriverReloadApplicationEvent() {
        super(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
    }

}
