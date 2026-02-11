package io.github.airiot.sdk.client.http.clients.driver;

import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.service.driver.DriverClient;
import io.github.airiot.sdk.client.service.driver.dto.Command;
import org.jspecify.annotations.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface DriverClientImpl extends DriverClient {

    @PostExchange("/driver/driver/command")
    @Override
    ResponseDTO<String> sendCommand(@NonNull @RequestBody Command command);

    default <T> ResponseDTO<T> sendCommand(@NonNull Class<T> tClass, @NonNull Command command) {
        ResponseDTO<String> response = this.sendCommand(command);
        if (!response.isSuccess() || !StringUtils.hasText(response.getData())) {
            return response.to();
        } else if (String.class.isAssignableFrom(tClass)) {
            return response.to((T) response.getData());
        }
        T data = CustomGson.GSON.fromJson(response.getData(), tClass);
        return response.to(data);
    }
}
