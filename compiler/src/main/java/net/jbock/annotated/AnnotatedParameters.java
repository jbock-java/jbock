package net.jbock.annotated;

import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.Optional;

public final class AnnotatedParameters extends AnnotatedMethod {

    private final ExecutableParameters parameters;

    private AnnotatedParameters(
            EnumName enumName,
            ExecutableParameters parameters,
            String paramLabel) {
        super(enumName, paramLabel);
        this.parameters = parameters;
    }

    static AnnotatedParameters createParameters(
            ExecutableParameters parameters,
            EnumName enumName) {
        String paramLabel = parameters.paramLabel()
                .orElseGet(() -> SnakeName.create(parameters.simpleName().toString())
                        .snake('_')
                        .toUpperCase(Locale.US));
        return new AnnotatedParameters(enumName, parameters, paramLabel);
    }

    @Override
    Executable executable() {
        return parameters;
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isParameters() {
        return true;
    }

    @Override
    Optional<AnnotatedOption> asAnnotatedOption() {
        return Optional.empty();
    }

    @Override
    Optional<AnnotatedParameter> asAnnotatedParameter() {
        return Optional.empty();
    }

    @Override
    Optional<AnnotatedParameters> asAnnotatedParameters() {
        return Optional.of(this);
    }
}
