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

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.google.gson.Gson;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

public class JsonLayout extends LayoutBase<ILoggingEvent> {

    private final static int initialBufferSize = 512;
    private final Gson gson = new Gson();
    private final ZoneId zoneId = ZonedDateTime.now().getZone();
    private final ThrowableProxyConverter throwableProxyConverter = new ThrowableProxyConverter();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(new DateTimeFormatterBuilder()
                    .appendValue(HOUR_OF_DAY, 2)
                    .appendLiteral(':')
                    .appendValue(MINUTE_OF_HOUR, 2)
                    .optionalStart()
                    .appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 2)
                    .optionalStart()
                    .appendFraction(NANO_OF_SECOND, 9, 9, true).toFormatter())
            .optionalStart()
            .appendOffset("+HH:MM", "GMT")
            .toFormatter();

    @Override
    public String doLayout(ILoggingEvent event) {
        if (!(event instanceof LoggingEventWithContext)) {
            throw new IllegalArgumentException("the logging event '" + event.getClass().getName() + "' is not a instance of LoggingEventWithContext");
        }

        LoggerContext context = ((LoggingEventWithContext) event).getContext();

        String lineInfo = null;
        if (event.getCallerData() != null && event.getCallerData().length > 1) {
            StackTraceElement element = event.getCallerData()[1];
            lineInfo = element.getClassName() + ":" + element.getLineNumber();
        }

        String key = context.getKey();

        String time = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), zoneId).format(DATE_TIME_FORMATTER);
        StringBuilder sb = new StringBuilder(initialBufferSize);
        sb.append("{")
                .append("\"logType\":").append("\"__syslog__\"").append(",")
                .append("\"key\":").append('"').append(key == null ? "" : key).append('"').append(",")
                .append("\"level\":").append('"').append(event.getLevel().levelStr).append('"').append(",")
                .append("\"time\":").append('"').append(time).append('"').append(",")
                .append("\"projectId\":").append('"').append(context.getProjectId()).append('"').append(",")
                .append("\"service\":").append('"').append(context.getService()).append('"').append(",")
                .append("\"module\":").append('"').append(context.getModule()).append('"').append(",");

        String traceId = context.getTraceId();
        String spanId = context.getSpanId();
        if (traceId != null && !traceId.isEmpty()) {
            sb.append("\"traceId\":").append('"').append(context.getTraceId()).append('"').append(",");
        }
        if (spanId != null && !spanId.isEmpty()) {
            sb.append("\"spanId\":").append('"').append(context.getSpanId()).append('"').append(",");
        }

        if (context.getData() != null) {
            sb.append("\"data\":{")
                    .append("\"__line__\":\"").append(lineInfo).append("\",")
                    .append("\"raw\":").append(gson.toJson(context.getData()))
                    .append("\"}");
        } else {
            sb.append("\"data\":").append("{\"__line__\":\"").append(lineInfo).append("\"}");
        }

        sb.append(",");

        if (event.getThrowableProxy() != null) {
            String exception = throwableProxyConverter.convert(event);
            exception = exception.replaceAll("\r\n", "\\\\r\\\\n");
            exception = exception.replaceAll("\n", "\\\\r\\\\n");
            sb.append("\"msg\":").append('"').append(event.getFormattedMessage()).append("\\\\r\\\\n")
                    .append(exception).append('"');
        } else {
            sb.append("\"msg\":").append('"').append(event.getFormattedMessage()).append('"');
        }

        sb.append("}");
        sb.append("\r");
        sb.append("\n");
        return sb.toString();
    }
}
