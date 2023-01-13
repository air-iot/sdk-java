package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.model.Event;

/**
 * 事件发送异常
 */
public class EventSenderException extends RuntimeException {

    /**
     * 事件
     */
    private final Event event;

    public Event getEvent() {
        return event;
    }

    public EventSenderException(Event event, String message) {
        super(message);
        this.event = event;
    }

    public EventSenderException(Event event, Throwable cause) {
        super(cause);
        this.event = event;
    }
    
    public EventSenderException(Event event, String message, Throwable cause) {
        super(message, cause);
        this.event = event;
    }
}
