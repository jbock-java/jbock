package net.jbock.annotated;

import net.jbock.Option;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.annotated.AnnotatedOption.createOption;
import static net.jbock.common.Constants.optionalString;

final class ExecutableOption extends Executable {

    private final Option option;

    ExecutableOption(
            ExecutableElement method,
            Option option,
            Optional<TypeElement> converter,
            String enumName) {
        super(method, converter, enumName);
        this.option = option;
    }

    @Override
    AnnotatedMethod<?> annotatedMethod() {
        return createOption(this);
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
