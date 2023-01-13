package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.handlers.RangeValueHandler;
import cn.airiot.sdk.driver.data.model.Field;
import cn.airiot.sdk.driver.data.model.Point;
import cn.airiot.sdk.driver.model.Tag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DefaultDataHandlerChain implements DataHandlerChain {

    private final List<DataHandler> handlers = new ArrayList<>();

    public DefaultDataHandlerChain(List<DataHandler> handlers) {
        this(handlers, true);
    }

    public DefaultDataHandlerChain(List<DataHandler> handlers, boolean registerDefaults) {
        if (registerDefaults) {
            this.registerDefaultHandlers();
        }
        handlers.sort(Comparator.comparing(DataHandler::getOrder));
        this.handlers.addAll(handlers);
    }

    /**
     * 注册
     */
    public void registerDefaultHandlers() {
        this.handlers.add(new RangeValueHandler());
    }

    @Override
    public Object handle(String nodeId, Tag tag, Object value) {
        if (handlers.isEmpty()) {
            return value;
        }

        Object newValue = value;
        for (DataHandler handler : handlers) {
            if (!handler.supports(nodeId, tag, newValue)) {
                continue;
            }

            newValue = handler.handle(nodeId, tag, newValue);
            if (newValue == null) {
                break;
            }
        }

        return newValue;
    }

    @Override
    public Point handle(Point point) {
        if (handlers.isEmpty()) {
            return point;
        }
        
        String deviceId = point.getId();
        List<Field> finalFields = new ArrayList<>(point.getFields().size());
        for (Field field : point.getFields()) {
            Object newValue = this.handle(deviceId, field.getTag(), field.getValue());
            if (newValue == null) {
                continue;
            }

            field.setValue(newValue);
            finalFields.add(field);
        }

        point.setFields(finalFields);
        return point;
    }
}
