package com.github.airiot.sdk.client.builder;

import java.util.Map;

@FunctionalInterface
public interface AggregateSegment {

    Map<String, Object> apply(String field, String alias);

}
