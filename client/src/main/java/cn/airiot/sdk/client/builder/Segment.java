package cn.airiot.sdk.client.builder;


@FunctionalInterface
public interface Segment {

    Object apply(Object value);

}
