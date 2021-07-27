package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.common.Descriptions;
import net.jbock.common.EnumName;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceOption;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public final class AnnotatedOption extends AnnotatedMethod {

    private final Option option;

    AnnotatedOption(
            ExecutableElement method,
            Option option,
            List<Modifier> accessModifiers) {
        super(method, accessModifiers);
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
    public Optional<String> label() {
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

    @Override
    public SourceMethod<?> sourceMethod(EnumName enumName, int numberOfParameters) {
        return new SourceOption(this, enumName);
    }
}
