package net.jbock.annotated;

import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.stream.Stream;

public final class AnnotatedParameters extends AnnotatedMethod {

    private final ExecutableParameters parameters;

    private AnnotatedParameters(
            String enumName,
            ExecutableParameters parameters,
            String paramLabel) {
        super(enumName, paramLabel);
        this.parameters = parameters;
    }

    static AnnotatedParameters createParameters(
            ExecutableParameters parameters,
            String enumName) {
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
    Stream<AnnotatedOption> asAnnotatedOption() {
        return Stream.empty();
    }

    @Override
    Stream<AnnotatedParameter> asAnnotatedParameter() {
        return Stream.empty();
    }

    @Override
    Stream<AnnotatedParameters> asAnnotatedParameters() {
        return Stream.of(this);
    }
}
