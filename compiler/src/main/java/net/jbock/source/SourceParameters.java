package net.jbock.source;

import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.Optional;

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

    public int index() {
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

    @Override
    public String paramLabel() {
        return parameters.label()
                .orElseGet(() -> SnakeName.create(methodName()).snake('_').toUpperCase(Locale.US));
    }

    @Override
    public boolean hasUnixName() {
        return false;
    }
}
