package net.jbock.annotated;

import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.Optional;

public final class AnnotatedParameter extends AnnotatedMethod {

    private final ExecutableParameter parameter;

    private AnnotatedParameter(
            EnumName enumName,
            ExecutableParameter parameter,
            String paramLabel) {
        super(enumName, paramLabel);
        this.parameter = parameter;
    }

    static AnnotatedParameter createParameter(
            ExecutableParameter parameter,
            EnumName enumName) {
        String paramLabel = parameter.paramLabel()
                .orElseGet(() -> SnakeName.create(parameter.simpleName().toString())
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

    @Override
    Optional<AnnotatedOption> asAnnotatedOption() {
        return Optional.empty();
    }

    @Override
    Optional<AnnotatedParameter> asAnnotatedParameter() {
        return Optional.of(this);
    }

    @Override
    Optional<AnnotatedParameters> asAnnotatedParameters() {
        return Optional.empty();
    }

    public int index() {
        return parameter.index();
    }
}
