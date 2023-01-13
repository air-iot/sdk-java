package cn.airiot.sdk.driver.listener;


import org.springframework.context.SmartLifecycle;

/**
 * 驱动事件监听器
 * <br>
 * 监听平台下发到驱动的事件, 并调用驱动实例执行相应的操作
 */
public interface DriverEventListener extends SmartLifecycle {
    
}
