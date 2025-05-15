package io.github.airiot.config;

import io.github.airiot.config.etcd.EtcdConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModeCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        List<AnnotationAttributes> allAnnotationAttributes = metadata.getAnnotations()
                .stream(ModelConditionOnProperty.class.getName())
                .filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
                .map(MergedAnnotation::asAnnotationAttributes).collect(Collectors.toList());
        List<ConditionMessage> noMatch = new ArrayList<>();
        List<ConditionMessage> match = new ArrayList<>();
        for (AnnotationAttributes annotationAttributes : allAnnotationAttributes) {
            ConditionOutcome outcome = determineOutcome(annotationAttributes, context.getEnvironment());
            (outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
        }
        if (!noMatch.isEmpty()) {
            return ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
        }
        return ConditionOutcome.match(ConditionMessage.of(match));
    }

    private ConditionOutcome determineOutcome(AnnotationAttributes annotationAttributes, PropertyResolver resolver) {
        ModeCondition.Spec spec = new ModeCondition.Spec(annotationAttributes);

        boolean matched = false;

        Boolean liteMode = resolver.getProperty("API.LITEMODE", Boolean.class);
        if(Boolean.TRUE.equals(liteMode)) {
            matched = spec.isMatch(ModelConditionOnProperty.LITE);
        } else {
            Boolean enabled = resolver.getProperty(EtcdConfigProperties.PREFIX + ".enabled", Boolean.class);
            if(enabled == null) {
                enabled = spec.matchIfMissing;
            }

            if(enabled) {
                matched = spec.isMatch(ModelConditionOnProperty.ETCD);
            }
        }

        if (matched) {
            return ConditionOutcome.match("matched: " + spec.mode);
        }

        return ConditionOutcome.noMatch("mismatched: " + spec.mode);
    }

    private static class Spec {

        private final String mode;
        private final boolean matchIfMissing;

        Spec(AnnotationAttributes annotationAttributes) {
            this.mode = annotationAttributes.getString("value").trim();
            this.matchIfMissing = annotationAttributes.getBoolean("matchIfMissing");
        }

        private boolean isMatch(String expectedMode) {
            return this.mode.equalsIgnoreCase(expectedMode);
        }

        public boolean isMatchIfMissing() {
            return matchIfMissing;
        }
    }
}
