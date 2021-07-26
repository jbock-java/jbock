package net.jbock.method;

import net.jbock.Parameter;
import net.jbock.common.Descriptions;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

final class ParameterAnnotation extends MethodAnnotation {

    private final Parameter parameter;

    ParameterAnnotation(Parameter parameter) {
        this.parameter = parameter;
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
    public boolean isParameter() {
        return true;
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.of(parameter.index());
    }

    @Override
    public List<String> names() {
        return List.of();
    }

    @Override
    public List<String> description() {
        return List.of(parameter.description());
    }
}
