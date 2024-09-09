package io.github.airiot.sdk.client.http.clients.driver;

import feign.RequestLine;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.driver.DriverClient;
import io.github.airiot.sdk.client.service.driver.dto.Command;
import org.jetbrains.annotations.NotNull;

public interface DriverFeignClientImpl extends DriverClient {

    @RequestLine("POST /driver/driver/command")
    @Override
    ResponseDTO<Object> sendCommand(@NotNull Command command);

}
