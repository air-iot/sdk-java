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

import io.github.airiot.sdk.driver.DriverApp;
import io.github.airiot.sdk.driver.config.DriverSingleConfig;
import io.github.airiot.sdk.driver.configuration.DriverAutoConfiguration;
import io.github.airiot.sdk.driver.data.DataSender;
import io.github.airiot.sdk.driver.listener.BatchCmd;
import io.github.airiot.sdk.driver.listener.Cmd;
import io.github.airiot.sdk.driver.model.Tag;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("unittest")
public class WarningTests {

    public static class Config {
    }

    public static class Command {
    }

    public static class MyTag extends Tag {
    }

    @Configuration
    @ImportAutoConfiguration(DriverAutoConfiguration.class)
    public static class TestConfiguration {

        @Bean
        public TestDriver driver(DataSender dataSender) {
            return new TestDriver(dataSender);
        }
    }

    public static class TestDriver implements DriverApp<DriverSingleConfig<Config>, Command, MyTag> {

        private final DataSender dataSender;

        public TestDriver(DataSender dataSender) {
            this.dataSender = dataSender;
            System.out.println("TestDriver init");
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public void start(DriverSingleConfig<Config> configDriverSingleConfig) {

        }

        @Override
        public void stop() {

        }

        @Override
        public Object run(Cmd<Command> request) {
            return null;
        }

        @Override
        public Object batchRun(BatchCmd<Command> request) {
            return null;
        }

        @Override
        public Object writeTag(Cmd<MyTag> request) {
            return null;
        }

        @Override
        public String schema() {
            return null;
        }
    }

    @Autowired
    private DataSender dataSender;

    @Test
    void createWarning() {
        Warning warning = Warning.builder()
                .tableId("opcda")
                .deviceId("opcda001")
                .ruleId("Table|opcda|opcda001|real4|low")
                .level("高")
                .warningTypes("1d345be4-2567-4764-7890-3ghj278vb342")
                .handle(true)
                .disableAlert()
                .field("real4", 123.456)
                .description("这是一条由驱动产生的告警信息")
                .build();

        Assertions.assertTrue(StringUtils.hasText(warning.getId()));
        Assertions.assertEquals(warning.getTable().getId(), "opcda");
        Assertions.assertEquals(warning.getTableData().getId(), "opcda001");
        Assertions.assertEquals(warning.getRuleId(), "Table|opcda|opcda001|real4|low");
        Assertions.assertEquals(warning.getLevel(), "高");
        Assertions.assertFalse(warning.isAlert());
        Assertions.assertTrue(warning.isHandle());
        Assertions.assertEquals(warning.getFields().get(0).getId(), "real4");
        Assertions.assertEquals(warning.getFields().get(0).getValue(), 123.456);
        Assertions.assertEquals(warning.getDescription(), "这是一条由驱动产生的告警信息");
    }


    @Test
    void sendWarning() {
        Warning warning = Warning.builder()
                .tableId("opcda")
                .deviceId("opcda001")
                .ruleId("Table|opcda|opcda001|real4|low")
                .level("高")
                .warningTypes("1d345be4-2567-4764-7890-3ghj278vb342")
                .handle(true)
                .disableAlert()
                .field("real4", 123.456)
                .description("这是一条由驱动产生的告警信息")
                .build();

        dataSender.sendWarning(warning);
    }

    @Test
    void sendWarningRecovery() {
        WarningRecovery recovery = new WarningRecovery(
                Collections.singletonList("3d76bc9e0e0e463e95e85c6e3ffa91f1"),
                new WarningRecovery.WarnRecoveryData(LocalDateTime.now(),
                        Collections.singletonList((WarningField.create("real4", 112.3)))
                )
        );

        this.dataSender.recoverWarning("opcda", "opcda001", recovery);
    }
}
