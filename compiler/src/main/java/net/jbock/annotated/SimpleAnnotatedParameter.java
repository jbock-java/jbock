package net.jbock.annotated;

import net.jbock.Parameter;
import net.jbock.common.EnumName;

import javax.lang.model.element.ExecutableElement;

import static net.jbock.annotated.AnnotatedParameter.createParameter;

final class SimpleAnnotatedParameter extends SimpleAnnotated {

    private final Parameter parameter;

    SimpleAnnotatedParameter(ExecutableElement method, Parameter parameter) {
        super(method);
        this.parameter = parameter;
    }

    @Override
    AnnotatedMethod annotatedMethod(EnumName enumName) {
        return createParameter(method(), enumName,
                converter(), parameter, accessModifiers());
    }
}
