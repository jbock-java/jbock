package net.jbock.annotated;

import net.jbock.common.SnakeName;

import java.util.Locale;

public final class AnnotatedParameter extends AnnotatedMethod {

    private final ExecutableParameter parameter;

    private AnnotatedParameter(
            String enumName,
            ExecutableParameter parameter,
            String paramLabel) {
        super(enumName, paramLabel);
        this.parameter = parameter;
    }

    static AnnotatedParameter createParameter(
            ExecutableParameter parameter,
            String enumName) {
        String paramLabel = parameter.paramLabel()
                .orElseGet(() -> SnakeName.create(parameter.simpleName())
                        .snake('_')
                        .toUpperCase(Locale.US));
        return new AnnotatedParameter(enumName,
                parameter, paramLabel);
    }

    @Override
    Executable executable() {
        return parameter;
    }

    @Override
    public boolean isParameter() {
        return true;
    }

    @Override
    public boolean isParameters() {
        return false;
    }

    public int index() {
        return parameter.index();
    }
}
