package cn.airiot.sdk.driver.data.impl;

import cn.airiot.sdk.driver.GlobalContext;
import cn.airiot.sdk.driver.configuration.properties.DriverDataProperties;
import cn.airiot.sdk.driver.data.AbstractDataSender;
import cn.airiot.sdk.driver.data.DataHandlerChain;
import cn.airiot.sdk.driver.data.LogSenderException;
import cn.airiot.sdk.driver.data.model.Point;
import cn.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * RabbitMQ Amqp 协议
 */
public class AmqpDataSender extends AbstractDataSender {

    private final Logger log = LoggerFactory.getLogger(AmqpDataSender.class);
    private final DriverDataProperties.Amqp amqpProperties;
    private final ThreadLocal<Channel> channel = ThreadLocal.withInitial(this::createChannel);
    private Connection connection;

    private Channel createChannel() {
        if (this.connection == null) {
            throw new IllegalStateException("AmqpDataSender 未连接");
        }

        try {
            return this.connection.createChannel();
        } catch (IOException e) {
            throw new IllegalStateException("创建 Amqp Channel 异常", e);
        }
    }

    public AmqpDataSender(String projectId, DataHandlerChain chain,
                          DriverDataProperties.Amqp amqpProperties,
                          GlobalContext globalContext,
                          DriverServiceGrpc.DriverServiceBlockingStub driverGrpcClient) {
        super(projectId, globalContext, chain, driverGrpcClient);
        this.amqpProperties = amqpProperties;
    }

    @Override
    public void start() {
        if (this.isRunning()) {
            return;
        }

        log.info("AmqpDataSender: 开始连接, Server[{}:{}]",
                this.amqpProperties.getHost(), this.amqpProperties.getPort());

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.amqpProperties.getHost());
        factory.setPort(this.amqpProperties.getPort());
        factory.setUsername(this.amqpProperties.getUsername());
        factory.setPassword(this.amqpProperties.getPassword());
        factory.setTopologyRecoveryEnabled(true);

        // 自动重连
        long reconnectIntervalMs = this.amqpProperties.getReconnectInterval().toMillis();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(Math.max(reconnectIntervalMs, 5000));

        try {
            this.connection = factory.newConnection();
            log.info("AmqpDataSender: 已连接");
        } catch (Exception e) {
            throw new IllegalStateException("AmqpDataSender: 连接失败", e);
        }
    }

    @Override
    public void stop() {
        if (this.connection == null) {
            return;
        }

        log.info("AmqpDataSender: 关闭中");
        try {
            this.connection.close(10000);
            log.info("AmqpDataSender: 已关闭");
        } catch (Exception e) {
            log.warn("AmqpDataSender: 断开连接异常:", e);
        }

        this.connection = null;
    }

    @Override
    public boolean isRunning() {
        return this.connection != null && this.connection.isOpen();
    }

    @Override
    public void doWritePoint(Point point) throws Exception {
        byte[] payload = this.encode(point);
        String routingKey = String.format("data.%s.%s.%s", this.projectId, point.getTable(), point.getId());
        channel.get().basicPublish("data", routingKey,
                false, false, MessageProperties.TEXT_PLAIN, payload);
    }

    @Override
    public void doWriteLog(String deviceId, String level, String message) throws LogSenderException {
        if (!this.isRunning()) {
            throw new LogSenderException(deviceId, level, message, "未连接或连接中断");
        }

        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        try {
            this.connection.createChannel().basicPublish("logs",
                    String.format("logs.%s.%s.%s", this.projectId, level, deviceId),
                    false, false,
                    MessageProperties.TEXT_PLAIN, payload);
        } catch (Exception e) {
            throw new LogSenderException(deviceId, level, message, e);
        }
    }
}
