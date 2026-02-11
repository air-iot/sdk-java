package io.github.airiot.sdk.client.http.configuration;

import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.*;
import io.github.airiot.sdk.client.http.clients.core.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.Arrays;
import java.util.List;

public class GraalvmRuntimeHits implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        List<Class<?>> classes = Arrays.asList(
                BatchInsertResult.class,
                InsertResult.class,
                MultiResponseDTO.class,
                ResponseDTO.class,
                Token.class,
                UpdateOrDeleteResult.class,
                Query.class
        );

        ReflectionHints reflectionHints = hints.reflection();
        for (Class<?> clazz : classes) {
            reflectionHints.registerType(clazz, builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            ));
        }

        List<Class<?>> clientTypes = Arrays.asList(
                AppClientImpl.class,
                DepartmentClientImpl.class,
                MediaLibraryClientImpl.class,
                RoleClientImpl.class,
                SystemVariableClientImpl.class,
                TableDataClientImpl.class,
                TableSchemaClientImpl.class,
                TimingDataClientImpl.class,
                UserClientImpl.class
        );

        for (Class<?> clientType : clientTypes) {
//            reflectionHints.registerType(clientType, builder -> {
//                builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);
//                builder.withMethod("proxyClassLookup", Collections.singletonList(TypeReference.of(MethodHandles.Lookup.class)),
//                        ExecutableMode.INVOKE);
//            });

            reflectionHints.registerType(clientType, builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));
        }
        
        hints.proxies().registerJdkProxy(clientTypes.toArray(new Class<?>[0]));
        hints.proxies()
                .registerJdkProxy(
                        org.springframework.aop.SpringProxy.class,
                        org.springframework.aop.framework.Advised.class,
                        org.springframework.core.DecoratingProxy.class
                );
    }
}
