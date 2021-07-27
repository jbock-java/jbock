package net.jbock.source;

import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.EnumName;

import java.util.Optional;
import java.util.OptionalInt;

public final class SourceParameter extends SourceMethod<AnnotatedParameter> {

    private final AnnotatedParameter parameter;

    public SourceParameter(
            AnnotatedParameter methodAnnotation,
            EnumName enumName) {
        super(enumName);
        this.parameter = methodAnnotation;
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.of(parameter.index());
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
}
