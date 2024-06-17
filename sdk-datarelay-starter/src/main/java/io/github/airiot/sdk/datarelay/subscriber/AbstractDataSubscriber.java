package io.github.airiot.sdk.datarelay.subscriber;

import io.github.airiot.sdk.client.builder.LatestDataQuery;
import io.github.airiot.sdk.client.service.core.TimingDataClient;
import io.github.airiot.sdk.client.service.core.dto.latest.LatestData;
import io.github.airiot.sdk.datarelay.DataRelayModules;
import io.github.airiot.sdk.logger.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public abstract class AbstractDataSubscriber implements DataSubscriber {

    private final Logger logger = LoggerFactory.withContext().module(DataRelayModules.DATA_SUBSCRIBER).getStaticLogger(AbstractDataSubscriber.class);

    protected final List<Subscription> subscriptions = new ArrayList<>();
    protected final List<DataListener> listeners = new ArrayList<>();

    protected ReadWriteLock subLock = new ReentrantReadWriteLock();
    protected ReadWriteLock listenerLock = new ReentrantReadWriteLock();

    protected final TimingDataClient timingDataClient;

    public AbstractDataSubscriber(TimingDataClient timingDataClient) {
        this.timingDataClient = timingDataClient;
    }

    /**
     * 连接成功后的回调
     */
    protected void onConnect() {
        logger.info("数据订阅: 连接已建立, 订阅数据");
        Lock lock = this.subLock.readLock();
        lock.lock();

        if (logger.isDebugEnabled()) {
            logger.debug("数据订阅: 订阅数据, {}", this.subscriptions);
        }

        try {
            this.onSubscribes(this.subscriptions);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public DeviceLatestData queryLatest(String tableId, String deviceId) {
        DeviceLatestData deviceData = new DeviceLatestData();
        deviceData.setTableId(tableId);
        deviceData.setDeviceId(deviceId);

        List<LatestData> data = this.timingDataClient.queryLatest(LatestDataQuery.create().allTags(tableId, deviceId));
        if (CollectionUtils.isEmpty(data)) {
            return deviceData;
        }

        List<DeviceLatestData.DeviceTagData> tags = data.stream()
                .map(d -> new DeviceLatestData.DeviceTagData(d.getTime(), d.getTagId(), d.getValue()))
                .collect(Collectors.toList());

        deviceData.setTags(tags);
        return deviceData;
    }

    @Override
    public List<DeviceLatestData> queryLatest(List<Subscription> subscriptions) {
        LatestDataQuery queries = LatestDataQuery.create();
        for (Subscription s : subscriptions) {
            queries.allTags(s.tableId, s.deviceId);
        }


        List<LatestData> data = this.timingDataClient.queryLatest(queries);
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }

        Map<String, DeviceLatestData> devices = new HashMap<>(subscriptions.size());
        for (LatestData datum : data) {
            String key = String.format("#T%s#D%s", datum.getTableId(), datum.getId());
            if (!devices.containsKey(key)) {
                DeviceLatestData device = new DeviceLatestData();
                device.setTableId(datum.getTableId());
                device.setDeviceId(datum.getId());
                device.setTags(new ArrayList<>());
                devices.put(key, device);
            }

            DeviceLatestData device = devices.get(key);
            device.getTags().add(new DeviceLatestData.DeviceTagData(datum.getTime(), datum.getTagId(), datum.getValue()));
        }

        return new ArrayList<>(devices.values());
    }

    @Override
    public void subscribe(List<Subscription> subscriptions) {
        Lock lock = this.subLock.writeLock();
        lock.lock();
        try {
            this.subscriptions.addAll(subscriptions);
            this.onSubscribes(subscriptions);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clearSubscriptions() {
        Lock lock = this.subLock.writeLock();
        lock.lock();
        try {
            this.onClearSubscriptions(this.subscriptions);
            this.subscriptions.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void listen(DataListener listener) {
        Lock lock = this.listenerLock.writeLock();
        lock.lock();
        try {
            this.listeners.add(listener);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() {
        Lock lock = this.subLock.writeLock();
        lock.lock();
        try {
            this.onClearSubscriptions(this.subscriptions);
            this.subscriptions.clear();
        } finally {
            lock.unlock();
        }
    }

    protected void deliver(DeviceData data) {
        Lock lock = this.listenerLock.readLock();
        lock.lock();

        try {
            if (this.listeners.isEmpty()) {
                logger.debug("下发设备数据: 未填加任何订阅器");
                return;
            }

            for (DataListener listener : this.listeners) {
                try {
                    listener.onMessage(data);
                } catch (Exception e) {
                    logger.error("下发设备数据: 表={},设备={}. 下发设备数据到 {} 失败", data.getTableId(), data.getDeviceId(), listener.getClass(), e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    protected void fill(String tableId, String deviceId, DeviceData data) {
        if (!StringUtils.hasText(tableId) || !StringUtils.hasText(deviceId)) {
            throw new IllegalArgumentException("填充设备数据信息失败, 表标识和设备编号不能为空");
        }

        if (data.getTime() == 0) {
            data.setTime(System.currentTimeMillis());
        }
        if (data.getFields() == null) {
            data.setFields(Collections.emptyMap());
        }
        if (data.getFieldTypes() == null) {
            data.setFieldTypes(Collections.emptyMap());
        }

        data.setTableId(tableId);
        data.setDeviceId(deviceId);
    }

    protected abstract void onSubscribes(List<Subscription> subscriptions);

    protected abstract void onClearSubscriptions(List<Subscription> subscriptions);
}
