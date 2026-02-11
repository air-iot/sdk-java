package io.github.airiot.sdk.driver.configuration;

import io.github.airiot.sdk.driver.ai.*;
import io.github.airiot.sdk.driver.config.*;
import io.github.airiot.sdk.driver.listener.*;
import io.github.airiot.sdk.driver.model.*;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;

public class GraalvmRuntimeHits implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
        List<Class<?>> classes = Arrays.asList(
                BasicSettings.class,
                BasicSettings.Network.class,
                BasicConfig.class,
                Device.class,
                DriverConfig.class,
                DriverSingleConfig.class,
                DriverSingleConfig.Model.class,
                Model.class,
                ErrorResult.class,
                HttpCmd.class,
                HttpBatchCmd.class,
                StatusResult.class,
                WebsocketSubscription.class,
                WebsocketSubscription.Subscription.class,
                WebsocketSubscription.SubscriptionResponse.class,
                WebsocketSubscription.DeviceStatusMessagePayload.class,
                BatchCmd.class,
                Cmd.class,
                Request.class,
                Response.class,
                ResultMsg.class,
                Result.class,
                Event.class,
                Field.class,
                FieldType.class,
                Point.class,
                Range.class,
                RunLog.class,
                Tag.class,
                TagValue.class,
                UpdateTableDTO.class
        );

        ReflectionHints reflectionHints = hints.reflection();
        for (Class<?> clazz : classes) {
            reflectionHints.registerType(clazz, builder -> builder.withMembers(MemberCategory.ACCESS_DECLARED_FIELDS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS));
        }
        
        hints.resources().registerResource(new ClassPathResource("schema.js"));
        hints.resources().registerResource(new ClassPathResource("schema-en.js"));
    }
}
