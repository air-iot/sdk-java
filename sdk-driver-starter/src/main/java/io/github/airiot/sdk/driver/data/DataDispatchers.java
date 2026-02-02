package io.github.airiot.sdk.driver.data;

import io.github.airiot.sdk.driver.model.Point;
import io.github.airiot.sdk.driver.model.RunLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataDispatchers implements DataDispatcher {

    private final Map<Class<?>, DataDispatcher> dispatchers = new ConcurrentHashMap<>();

    public void register(DataDispatcher dispatcher) {
        this.dispatchers.putIfAbsent(dispatcher.getClass(), dispatcher);
    }

    public void unregister(DataDispatcher dispatcher) {
        this.dispatchers.remove(dispatcher.getClass());
    }

    @Override
    public void writePoint(Point point) {
        for (DataDispatcher dispatcher : this.dispatchers.values()) {
            dispatcher.writePoint(point);
        }
    }

    @Override
    public void writeRunLog(RunLog runLog) {
        for (DataDispatcher dispatcher : this.dispatchers.values()) {
            dispatcher.writeRunLog(runLog);
        }
    }

    @Override
    public void writeLog(String tableId, String deviceId, String level, String msg) {
        for (DataDispatcher dispatcher : this.dispatchers.values()) {
            dispatcher.writeLog(tableId, deviceId, level, msg);
        }
    }
}
