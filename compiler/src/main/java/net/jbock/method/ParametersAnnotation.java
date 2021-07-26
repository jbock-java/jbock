package net.jbock.method;

import net.jbock.Parameters;
import net.jbock.common.Descriptions;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

final class ParametersAnnotation extends MethodAnnotation {

    private final Parameters parameters;

    ParametersAnnotation(ExecutableElement sourceMethod, Parameters parameters) {
        super(sourceMethod);
        this.parameters = parameters;
    }

    @Override
    public Optional<String> descriptionKey() {
        return Descriptions.optionalString(parameters.descriptionKey());
    }

    @Override
    public Optional<String> paramLabel() {
        return Descriptions.optionalString(parameters.paramLabel());
    }

    @Override
    public boolean isParameters() {
        return true;
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.empty();
    }

    @Override
    public List<String> names() {
        return List.of();
    }

    @Override
    public List<String> description() {
        return List.of(parameters.description());
    }
}
