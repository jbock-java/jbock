package net.jbock.annotated;

import net.jbock.VarargsParameter;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.annotated.AnnotatedParameters.createParameters;
import static net.jbock.common.Constants.optionalString;

final class ExecutableParameters extends Executable {

    private final VarargsParameter parameters;

    ExecutableParameters(
            ExecutableElement method,
            VarargsParameter parameters,
            Optional<TypeElement> converter) {
        super(method, converter);
        this.parameters = parameters;
    }

    @Override
    AnnotatedMethod annotatedMethod(
            SourceElement sourceElement,
            String enumName) {
        return createParameters(this, enumName);
    }

    @Override
    Optional<String> descriptionKey() {
        return optionalString(parameters.descriptionKey());
    }

    @Override
    List<String> description() {
        return List.of(parameters.description());
    }

    Optional<String> paramLabel() {
        return optionalString(parameters.paramLabel());
    }
}
