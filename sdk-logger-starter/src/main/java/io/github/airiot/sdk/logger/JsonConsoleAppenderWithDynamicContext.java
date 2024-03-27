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

public class JsonConsoleAppenderWithDynamicContext extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final Appender<ILoggingEvent> delegate;
    private final LoggerContext context;
    private final LoggerContext pContext;

    public JsonConsoleAppenderWithDynamicContext(LoggerContext context, Appender<ILoggingEvent> delegate) {
        this.context = context;
        this.pContext = context.getParent();
        this.delegate = delegate;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        LoggerContext current = LoggerContexts.getContext();
        LoggerContext parent = current.getParent();

        // 如果当前上下文相同或者已经存在父级关系, 说明当前线程栈中没有添加新的日志上下文
        if (this.context == current || this.context == parent) {
            return;
        }
        
        current.setParent(this.context);
        if (parent != null) {
            this.context.setParent(parent);
        }

        try {
            this.delegate.doAppend(new LoggingEventWithContext(current, eventObject));
        } finally {
            if (parent != null) {
                current.setParent(parent);
            }
            this.context.setParent(this.pContext);
        }
    }
}
