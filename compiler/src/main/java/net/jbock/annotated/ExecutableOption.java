package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.common.EnumName;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.annotated.AnnotatedOption.createOption;
import static net.jbock.common.Constants.optionalString;

final class ExecutableOption extends Executable {

    private final Option option;

    ExecutableOption(ExecutableElement method, Option option) {
        super(method);
        this.option = option;
    }

    @Override
    AnnotatedMethod annotatedMethod(EnumName enumName) {
        return createOption(this, enumName);
    }

    @Override
    Optional<String> descriptionKey() {
        return optionalString(option.descriptionKey());
    }

    @Override
    List<String> description() {
        return List.of(option.description());
    }

    List<String> names() {
        return List.of(option.names());
    }

    Optional<String> paramLabel() {
        return optionalString(option.paramLabel());
    }
}
