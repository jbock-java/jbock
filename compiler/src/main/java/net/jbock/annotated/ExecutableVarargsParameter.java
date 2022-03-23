package net.jbock.annotated;

import net.jbock.VarargsParameter;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.annotated.AnnotatedVarargsParameter.createVarargsParameter;
import static net.jbock.common.Constants.optionalString;

final class ExecutableVarargsParameter extends Executable {

    private final VarargsParameter parameter;

    ExecutableVarargsParameter(
            ExecutableElement method,
            VarargsParameter parameter,
            Optional<TypeElement> converter) {
        super(method, converter);
        this.parameter = parameter;
    }

    @Override
    AnnotatedMethod annotatedMethod(
            SourceElement sourceElement,
            String enumName) {
        return createVarargsParameter(this, enumName);
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
}