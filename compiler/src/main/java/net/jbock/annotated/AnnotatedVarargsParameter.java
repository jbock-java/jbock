package net.jbock.annotated;

import net.jbock.common.SnakeName;

import java.util.Locale;

public final class AnnotatedVarargsParameter extends AnnotatedMethod {

    private final ExecutableVarargsParameter parameters;

    private AnnotatedVarargsParameter(
            String enumName,
            ExecutableVarargsParameter parameters,
            String paramLabel) {
        super(enumName, paramLabel);
        this.parameters = parameters;
    }

    static AnnotatedVarargsParameter createVarargsParameter(
            ExecutableVarargsParameter parameters,
            String enumName) {
        String paramLabel = parameters.paramLabel()
                .orElseGet(() -> SnakeName.create(parameters.simpleName())
                        .snake('_')
                        .toUpperCase(Locale.US));
        return new AnnotatedVarargsParameter(enumName, parameters, paramLabel);
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
}
