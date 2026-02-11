package io.github.airiot.sdk.client.http;

import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.http.annotation.GetObjectParams;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

public class CustomQueryMethodArgumentResolver implements HttpServiceArgumentResolver {

    @Override
    public boolean resolve(@Nullable Object argument,
                           @NonNull MethodParameter parameter,
                           HttpRequestValues.@NonNull Builder requestValues) {
        if(!parameter.hasParameterAnnotation(GetObjectParams.class)) {
            return false;
        }

        GetObjectParams params = parameter.getParameterAnnotation(GetObjectParams.class);
        String paramName = params.value();
        if(!StringUtils.hasText(paramName)) {
            paramName = parameter.getExecutable().getName();
        }

        if(argument == null) {
            requestValues.addRequestParameter(paramName, "");
        } else {
            String arg = CustomGson.GSON.toJson(argument);
            requestValues.addRequestParameter(paramName, arg);
        }

        return true;
    }
}
