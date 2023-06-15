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

package io.github.airiot.sdk.client.service.core.dto.timing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BuiltinTimingDataSeries {

    private String name;
    private Map<String, String> tags;
    private List<String> columns;
    private List<List<Object>> values;

    public String getName() {
        return name;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<Object>> getValues() {
        return values;
    }

    public TimingDataSeries parse() {
        if (this.name == null || this.columns == null || this.values == null) {
            return null;
        }

        List<TimingDataSeries.Value> values = new ArrayList<>(this.values.size());
        this.values.stream()
                .map(v -> new TimingDataSeries.Value(this.columns, v))
                .forEach(values::add);
        return new TimingDataSeries(this.name,
                this.tags == null ? Collections.emptyMap() : this.tags,
                this.columns, values);
    }
}
