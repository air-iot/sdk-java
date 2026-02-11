package io.github.airiot.sdk.client.service.driver;

import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.PlatformClient;
import io.github.airiot.sdk.client.service.driver.dto.Command;

/**
 * 驱动管理客户端
 */
public interface DriverClient extends PlatformClient {

    /**
     * 发送指令
     *
     * @return 指令发送结果
     */
    ResponseDTO<String> sendCommand(Command command);

}
