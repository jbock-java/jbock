package net.jbock.source;

import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.Optional;

public final class SourceParameters extends SourceMethod<AnnotatedParameters> {

    private final AnnotatedParameters parameters;
    private final String paramLabel;

    private SourceParameters(
            AnnotatedParameters parameters,
            EnumName enumName, String paramLabel) {
        super(enumName);
        this.parameters = parameters;
        this.paramLabel = paramLabel;
    }

    public static SourceParameters create(
            AnnotatedParameters parameters,
            EnumName enumName) {
        String paramLabel = parameters.label()
                .orElseGet(() -> SnakeName.create(parameters.method().getSimpleName().toString())
                        .snake('_')
                        .toUpperCase(Locale.US));
        return new SourceParameters(parameters, enumName, paramLabel);
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
        return paramLabel;
    }

    @Override
    public boolean hasUnixName() {
        return false;
    }
}
