package io.github.airiot.sdk.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class GraalvmRuntimeHits implements RuntimeHintsRegistrar {
    
    @Override
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        hints.reflection()
                .registerType(JsonLoggerApplicationRunListener.class, builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.ACCESS_DECLARED_FIELDS))
                .registerType(JsonConsoleAppenderWithContext.class, builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.ACCESS_DECLARED_FIELDS))
                .registerType(JsonConsoleAppenderWithDynamicContext.class, builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.ACCESS_DECLARED_FIELDS))
        ;

        hints.reflection()
                .registerType(ch.qos.logback.classic.Logger.class, builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS))
                .registerType(ILoggingEvent.class, builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS))
                .registerType(ch.qos.logback.classic.LoggerContext.class, builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));

        hints.serialization()
                .registerType(ch.qos.logback.classic.model.ConfigurationModel.class)
                .registerType(ch.qos.logback.classic.model.LoggerModel.class)
                .registerType(ch.qos.logback.classic.model.RootLoggerModel.class)
                .registerType(ch.qos.logback.core.model.AppenderModel.class)
                .registerType(ch.qos.logback.core.model.AppenderRefModel.class)
                .registerType(ch.qos.logback.core.model.ComponentModel.class)
                .registerType(ch.qos.logback.core.model.ConversionRuleModel.class)
                .registerType(ch.qos.logback.core.model.ImplicitModel.class)
                .registerType(ch.qos.logback.core.model.IncludeModel.class)
                .registerType(ch.qos.logback.core.model.Model.class)
                .registerType(ch.qos.logback.core.model.NamedComponentModel.class)
                .registerType(ch.qos.logback.core.model.NamedModel.class)
                .registerType(ch.qos.logback.core.model.PropertyModel.class)
                .registerType(ch.qos.logback.core.model.ResourceModel.class)
        ;
    }
}
