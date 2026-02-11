package io.github.airiot.sdk.logger;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(GraalvmRuntimeHits.class)
public class AiriotLoggerAutoConfiguration {

}
