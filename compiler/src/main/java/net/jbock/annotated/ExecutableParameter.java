package net.jbock.annotated;

import net.jbock.Parameter;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.annotated.AnnotatedParameter.createParameter;
import static net.jbock.common.Constants.optionalString;

final class ExecutableParameter extends Executable {

    private final Parameter parameter;

    ExecutableParameter(
            ExecutableElement method,
            Parameter parameter,
            Optional<TypeElement> converter,
            String enumName) {
        super(method, converter, enumName);
        this.parameter = parameter;
    }

    @Override
    AnnotatedMethod<?> annotatedMethod() {
        return createParameter(this);
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
