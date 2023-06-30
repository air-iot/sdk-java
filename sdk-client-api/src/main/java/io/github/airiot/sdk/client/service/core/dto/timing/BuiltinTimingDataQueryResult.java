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

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuiltinTimingDataQueryResult {

    private List<BuiltinTimingDataSeriesBody> results;

    public List<BuiltinTimingDataSeriesBody> getResults() {
        return results;
    }

    public List<TimingData> parse() {
        if (this.results == null || this.results.isEmpty()) {
            return Collections.emptyList();
        }

        List<TimingData> timingData = new ArrayList<>(this.results.size());
        for (BuiltinTimingDataSeriesBody result : this.results) {
            if (CollectionUtils.isEmpty(result.series)) {
                continue;
            }
            List<TimingDataSeries> series = new ArrayList<>(result.series.size());
            for (BuiltinTimingDataSeries s : result.getSeries()) {
                if (s == null) {
                    continue;
                }

                TimingDataSeries ss = s.parse();
                if (ss != null) {
                    series.add(ss);
                }
            }
            timingData.add(new TimingData(series));
        }

        return timingData;
    }

    public static class BuiltinTimingDataSeriesBody {
        private List<BuiltinTimingDataSeries> series;

        public List<BuiltinTimingDataSeries> getSeries() {
            return series;
        }
    }
}
