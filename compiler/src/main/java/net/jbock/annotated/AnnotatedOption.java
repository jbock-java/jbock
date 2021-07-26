package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.common.Descriptions;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;

public final class AnnotatedOption extends AnnotatedMethod {

    private final Option option;

    AnnotatedOption(ExecutableElement sourceMethod, Option option) {
        super(sourceMethod);
        this.option = option;
    }

    @Override
    public boolean isParameter() {
        return false;
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
    public List<String> names() {
        return List.of(option.names());
    }

    @Override
    public List<String> description() {
        return List.of(option.description());
    }
}
