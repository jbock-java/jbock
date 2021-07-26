package net.jbock.annotated;

import net.jbock.Parameter;
import net.jbock.common.Descriptions;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;

public final class AnnotatedParameter extends AnnotatedMethod {

    private final Parameter parameter;

    AnnotatedParameter(ExecutableElement sourceMethod, Parameter parameter) {
        super(sourceMethod);
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
    public Optional<String> paramLabel() {
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

    public int index() {
        return parameter.index();
    }
}
