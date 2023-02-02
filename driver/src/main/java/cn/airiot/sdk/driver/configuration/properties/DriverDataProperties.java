package cn.airiot.sdk.driver.configuration.properties;


import cn.airiot.sdk.driver.data.DataSenderException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;


/**
 * 驱动采集的数据及事件上报配置
 */
@ConfigurationProperties(prefix = "airiot.driver.data")
public class DriverDataProperties {

    /**
     * 连接断开时的数据处理策略
     */
    private DataHandlePolicyOnConnectLost policy = DataHandlePolicyOnConnectLost.EXCEPTION;
    /**
     * 连接断开时, 输出到日志的等级
     * <br>
     * 只有 {@link #policy} 为 {@link DataHandlePolicyOnConnectLost#LOG} 时有效
     */
    private LogLevel logLevel = LogLevel.ERROR;


    public DataHandlePolicyOnConnectLost getPolicy() {
        return policy;
    }

    public void setPolicy(DataHandlePolicyOnConnectLost policy) {
        this.policy = policy;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }


    /**
     * 连接断开时的数据处理策略
     */
    public enum DataHandlePolicyOnConnectLost {
        /**
         * 抛出 {@link DataSenderException} 异常
         */
        EXCEPTION,
        /**
         * 输出到日志
         */
        LOG,
        /**
         * 丢弃
         */
        DISCARD;
    }
}
