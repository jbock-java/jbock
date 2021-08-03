package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.common.EnumName;

import javax.lang.model.element.ExecutableElement;

import static net.jbock.annotated.AnnotatedOption.createOption;

final class SimpleAnnotatedOption extends SimpleAnnotated {

    private final Option option;

    SimpleAnnotatedOption(ExecutableElement method, Option option) {
        super(method);
        this.option = option;
    }

    @Override
    AnnotatedMethod annotatedMethod(EnumName enumName) {
        return createOption(method(), enumName,
                converter(), option, accessModifiers());
    }
}
