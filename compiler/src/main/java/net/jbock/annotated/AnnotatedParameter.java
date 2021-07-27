package net.jbock.annotated;

import net.jbock.Parameter;
import net.jbock.common.Descriptions;
import net.jbock.common.EnumName;
import net.jbock.method.ParameterAnnotation;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceParameter;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;

public final class AnnotatedParameter extends AnnotatedMethod {

    private final Parameter parameter;

    AnnotatedParameter(ExecutableElement method, Parameter parameter) {
        super(method);
        this.parameter = parameter;
    }

    @Override
    public boolean isParameter() {
        return true;
    }

    @Override
    public Optional<String> descriptionKey() {
        return Descriptions.optionalString(parameter.descriptionKey());
    }

    @Override
    public Optional<String> label() {
        return Descriptions.optionalString(parameter.paramLabel());
    }

    @Override
    public boolean isParameters() {
        return false;
    }

    @Override
    public List<String> names() {
        return List.of();
    }

    @Override
    public List<String> description() {
        return List.of(parameter.description());
    }

    @Override
    public SourceMethod<?> sourceMethod(EnumName enumName, int numberOfParameters) {
        ParameterAnnotation annotation = new ParameterAnnotation(this);
        return new SourceParameter(annotation, enumName);
    }

    public int index() {
        return parameter.index();
    }
}
