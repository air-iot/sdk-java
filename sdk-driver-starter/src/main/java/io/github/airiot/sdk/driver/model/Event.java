/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.airiot.sdk.driver.model;

import io.github.airiot.sdk.driver.grpc.driver.DriverServiceGrpc;

/**
 * 事件
 * <br>
 *
 * @see DriverServiceGrpc#getEventMethod()
 */
public class Event {
    /**
     * 资产ID
     */
    private String id;
    /**
     * 表ID
     */
    private String table;
    /**
     * 事件ID
     */
    private String eventId;
    /**
     * 事件产生的时间(时间戳)
     */
    private long time;
    /**
     * 事件数据
     */
    private Object data;

    public Event() {
    }

    public Event(String id, String eventId, long time, Object data) {
        this.id = id;
        this.eventId = eventId;
        this.time = time;
        this.data = data;
    }

    public Event(String id, String table, String eventId, long time, Object data) {
        this.id = id;
        this.table = table;
        this.eventId = eventId;
        this.time = time;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", table='" + table + '\'' +
                ", eventId='" + eventId + '\'' +
                ", time=" + time +
                ", data=" + data +
                '}';
    }
}
