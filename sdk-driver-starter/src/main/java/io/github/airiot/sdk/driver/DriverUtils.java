package io.github.airiot.sdk.driver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DriverUtils {

    public static Type[] parseDriverAppGenericTypes(DriverApp<?, ?, ?> driverApp) {
        for (Type type : driverApp.getClass().getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }

            ParameterizedType pType = (ParameterizedType) type;
            if (!((Class<?>) pType.getRawType()).isAssignableFrom(DriverApp.class)) {
                continue;
            }

            return pType.getActualTypeArguments();
        }

        throw new IllegalStateException("the type " + driverApp.getClass().getName() + " is not implements interface " + DriverApp.class.getName());
    }
}
