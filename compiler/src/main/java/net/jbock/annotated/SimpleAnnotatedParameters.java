package net.jbock.annotated;

import net.jbock.Parameters;
import net.jbock.common.EnumName;

import javax.lang.model.element.ExecutableElement;

import static net.jbock.annotated.AnnotatedParameters.createParameters;

final class SimpleAnnotatedParameters extends SimpleAnnotated {

    private final Parameters parameters;

    SimpleAnnotatedParameters(ExecutableElement method, Parameters parameters) {
        super(method);
        this.parameters = parameters;
    }

    @Override
    AnnotatedMethod annotatedMethod(EnumName enumName) {
        return createParameters(method(), enumName,
                converter(), parameters, accessModifiers());
    }
}
