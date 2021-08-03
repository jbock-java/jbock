package net.jbock.annotated;

import net.jbock.Parameter;
import net.jbock.common.EnumName;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.annotated.AnnotatedParameter.createParameter;
import static net.jbock.common.Constants.optionalString;

final class ExecutableParameter extends Executable {

    private final Parameter parameter;

    ExecutableParameter(ExecutableElement method, Parameter parameter) {
        super(method);
        this.parameter = parameter;
    }

    @Override
    AnnotatedMethod annotatedMethod(EnumName enumName) {
        return createParameter(this, enumName);
    }

    @Override
    Optional<String> descriptionKey() {
        return optionalString(parameter.descriptionKey());
    }

    @Override
    List<String> description() {
        return List.of(parameter.description());
    }

    Optional<String> paramLabel() {
        return optionalString(parameter.paramLabel());
    }

    int index() {
        return parameter.index();
    }
}
