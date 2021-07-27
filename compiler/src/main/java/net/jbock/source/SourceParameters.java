package net.jbock.source;

import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.EnumName;

import java.util.Optional;
import java.util.OptionalInt;

public final class SourceParameters extends SourceMethod<AnnotatedParameters> {

    private final int index;
    private final AnnotatedParameters parameters;

    public SourceParameters(
            int index,
            AnnotatedParameters parameters,
            EnumName enumName) {
        super(enumName);
        this.parameters = parameters;
        this.index = index;
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.of(index);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public AnnotatedParameters annotatedMethod() {
        return parameters;
    }

    @Override
    public Optional<SourceOption> asAnnotatedOption() {
        return Optional.empty();
    }

    @Override
    public Optional<SourceParameter> asAnnotatedParameter() {
        return Optional.empty();
    }

    @Override
    public Optional<SourceParameters> asAnnotatedParameters() {
        return Optional.of(this);
    }
}
