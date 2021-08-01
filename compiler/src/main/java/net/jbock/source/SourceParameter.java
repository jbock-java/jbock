package net.jbock.source;

import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.Optional;

public final class SourceParameter extends SourceMethod<AnnotatedParameter> {

    private final String paramLabel;

    private SourceParameter(
            AnnotatedParameter parameter,
            EnumName enumName,
            String paramLabel) {
        super(parameter, enumName);
        this.paramLabel = paramLabel;
    }

    public static SourceParameter create(
            AnnotatedParameter parameter,
            EnumName enumName) {
        String paramLabel = parameter.label()
                .orElseGet(() -> SnakeName.create(parameter.method().getSimpleName().toString())
                        .snake('_')
                        .toUpperCase(Locale.US));
        return new SourceParameter(parameter, enumName, paramLabel);
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
        return paramLabel;
    }
}
