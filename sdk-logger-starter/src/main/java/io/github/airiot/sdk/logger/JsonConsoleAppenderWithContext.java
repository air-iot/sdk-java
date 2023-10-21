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

package io.github.airiot.sdk.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class JsonConsoleAppenderWithContext extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final Appender<ILoggingEvent> delegate;
    private final LoggerContext context;

    public JsonConsoleAppenderWithContext(LoggerContext context, Appender<ILoggingEvent> delegate) {
        this.context = context;
        this.delegate = delegate;
    }
    
    @Override
    protected void append(ILoggingEvent eventObject) {
        this.delegate.doAppend(new LoggingEventWithContext(this.context, eventObject));
    }
}
