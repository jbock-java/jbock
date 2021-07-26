package net.jbock.method;

import net.jbock.Option;
import net.jbock.common.Descriptions;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

final class OptionAnnotation extends MethodAnnotation {

    private final Option option;

    OptionAnnotation(ExecutableElement sourceMethod, Option option) {
        super(sourceMethod);
        this.option = option;
    }

    @Override
    public Optional<String> descriptionKey() {
        return Descriptions.optionalString(option.descriptionKey());
    }

    @Override
    public Optional<String> paramLabel() {
        return Descriptions.optionalString(option.paramLabel());
    }

    @Override
    public boolean isParameters() {
        return false;
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
        return List.of(option.names());
    }

    @Override
    public List<String> description() {
        return List.of(option.description());
    }
}
