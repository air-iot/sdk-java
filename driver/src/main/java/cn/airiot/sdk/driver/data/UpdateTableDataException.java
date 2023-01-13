package cn.airiot.sdk.driver.data;

import cn.airiot.sdk.driver.data.model.UpdateTableDTO;

/**
 * 更新设备属性异常
 */
public class UpdateTableDataException extends RuntimeException {

    private final UpdateTableDTO data;

    public UpdateTableDataException(UpdateTableDTO data, String message) {
        super(message);
        this.data = data;
    }

    public UpdateTableDataException(UpdateTableDTO data, Throwable cause) {
        super(cause);
        this.data = data;
    }
}
