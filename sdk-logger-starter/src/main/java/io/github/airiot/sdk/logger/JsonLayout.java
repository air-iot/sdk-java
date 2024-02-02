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
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.LayoutBase;
import com.google.gson.Gson;
import io.github.airiot.sdk.logger.suggestion.SuggestionException;
import org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.time.temporal.ChronoField.*;

public class JsonLayout extends LayoutBase<ILoggingEvent> {

    private final static int initialBufferSize = 512;
    private final Gson gson = new Gson();
    private final ZoneId zoneId = ZonedDateTime.now().getZone();
    private final ExtendedWhitespaceThrowableProxyConverter throwableProxyConverter = new ExtendedWhitespaceThrowableProxyConverter();

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
    public void start() {
        super.start();
        throwableProxyConverter.setOptionList(Collections.singletonList("10"));
        throwableProxyConverter.start();
    }

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

        Map<String, Object> keys = context.getRefData(true);

        String time = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), zoneId).format(DATE_TIME_FORMATTER);
        StringBuilder sb = new StringBuilder(initialBufferSize);
        sb.append("{")
                .append("\"logType\":").append("\"__syslog__\"").append(",")
                .append("\"level\":").append('"').append(event.getLevel().levelStr).append('"').append(",")
                .append("\"time\":").append('"').append(time).append('"').append(",")
                .append("\"projectId\":").append('"').append(context.getProjectId()).append('"').append(",")
                .append("\"service\":").append('"').append(context.getService()).append('"').append(",")
                .append("\"module\":").append('"').append(context.getModule()).append('"').append(",");

        if (keys != null && !keys.isEmpty()) {
            for (Map.Entry<String, Object> entry : keys.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }

                sb.append('"').append(key).append('"').append(":");

                if (value instanceof Number) {
                    sb.append(value);
                } else {
                    sb.append('"').append(value).append('"');
                }

                sb.append(",");
            }
        }

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
        sb.append("\"msg\":").append('"').append(event.getFormattedMessage()).append('"');

        String detail = "";
        Optional<Object> detailValue = context.getRefData(LoggerContext.DETAIL_KEY);
        if (detailValue.isPresent()) {
            detail = String.valueOf(detailValue.get());
        }

        String suggest = "";
        Optional<Object> suggestValue = context.getRefData(LoggerContext.SUGGESTION_KEY);
        if (suggestValue.isPresent()) {
            suggest = String.valueOf(suggestValue.get());
        }

        if (event.getThrowableProxy() != null) {
            // 建议信息
            IThrowableProxy proxy = event.getThrowableProxy();
            if (proxy instanceof ThrowableProxy) {
                Throwable cause = ((ThrowableProxy) proxy).getThrowable();
                if (cause instanceof SuggestionException) {
                    if (!suggest.isEmpty()) {
                        suggest += "," + ((SuggestionException) cause).getSuggestion();
                    } else {
                        suggest = ((SuggestionException) cause).getSuggestion();
                    }
                }
            }

            // 将手动设置的 detail 和异常信息拼接到一起
            String exception = throwableProxyConverter.convert(event);
            exception = exception.replaceAll("\r\n", "\\\\r\\\\n");
            exception = exception.replaceAll("\n", "\\\\r\\\\n");
            exception = exception.replaceAll("\t", "");

            if (detail.isEmpty()) {
                detail = exception;
            } else {
                detail = "\\r\\n" + exception;
            }
        }

        if (!detail.isEmpty()) {
            sb.append(",\"" + LoggerContext.DETAIL_KEY + "\":").append('"').append(detail).append('"');
        }

        if (!suggest.isEmpty()) {
            sb.append(",\"" + LoggerContext.SUGGESTION_KEY + "\":")
                    .append('"')
                    .append(suggest)
                    .append('"');
        }

        sb.append("}");
        sb.append("\r\n");
        return sb.toString();
    }
}
