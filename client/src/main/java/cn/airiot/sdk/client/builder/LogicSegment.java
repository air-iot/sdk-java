package cn.airiot.sdk.client.builder;


@FunctionalInterface
public interface LogicSegment {
    
    Object apply(Object... value);

}
