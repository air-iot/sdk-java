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

package io.github.airiot.sdk.driver.data.warning;


import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 报警恢复信息
 * <br>
 * 即由报警状态变为正常状态的信息
 */
public class WarningRecovery {
    /**
     * 报警ID列表
     */
    private List<String> id;
    /**
     * 恢复报警的数据点信息
     */
    private WarnRecoveryData data;

    public List<String> getId() {
        return id;
    }

    public WarnRecoveryData getData() {
        return data;
    }

    public WarningRecovery(List<String> id, WarnRecoveryData data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public String toString() {
        return "WarningRecovery{" +
                "id=" + id +
                ", data=" + data +
                '}';
    }

    /**
     * 恢复报警的数据点信息
     */
    public static class WarnRecoveryData {
        @SerializedName("recoveryTime")
        private final ZonedDateTime time;
        @SerializedName("recoveryFields")
        private final List<WarningField> fields;

        public ZonedDateTime getTime() {
            return time;
        }

        public List<WarningField> getFields() {
            return fields;
        }

        public WarnRecoveryData(ZonedDateTime time, List<WarningField> fields) {
            this.time = time;
            this.fields = fields;
        }

        public WarnRecoveryData(LocalDateTime time, List<WarningField> fields) {
            this(ZonedDateTime.of(time, ZoneOffset.systemDefault()), fields);
        }

        @Override
        public String toString() {
            return "WarnRecoveryData{" +
                    "time=" + time +
                    ", fields=" + fields +
                    '}';
        }
    }
}
