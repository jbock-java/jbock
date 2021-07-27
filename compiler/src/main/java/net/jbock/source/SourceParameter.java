package net.jbock.source;

import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.Optional;

public final class SourceParameter extends SourceMethod<AnnotatedParameter> {

    private final AnnotatedParameter parameter;

    public SourceParameter(
            AnnotatedParameter methodAnnotation,
            EnumName enumName) {
        super(enumName);
        this.parameter = methodAnnotation;
    }

    @Override
    public AnnotatedParameter annotatedMethod() {
        return parameter;
    }

    @Override
    public Optional<SourceOption> asAnnotatedOption() {
        return Optional.empty();
    }

    @Override
    public Optional<SourceParameter> asAnnotatedParameter() {
        return Optional.of(this);
    }

    @Override
    public Optional<SourceParameters> asAnnotatedParameters() {
        return Optional.empty();
    }

    @Override
    public String paramLabel() {
        return parameter.label()
                .orElseGet(() -> SnakeName.create(methodName()).snake('_').toUpperCase(Locale.US));
    }
}
