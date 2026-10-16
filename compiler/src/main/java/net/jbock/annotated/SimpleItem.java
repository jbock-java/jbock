package net.jbock.annotated;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;

sealed interface SimpleItem permits SimpleOption, SimpleParameter {

    ExecutableElement method();

    String safeName();

    static SimpleItem create(
            ExecutableElement method,
            Annotation annotation,
            String safeName) {
        return switch (annotation) {
            case net.jbock.Option option -> new SimpleOption(method, option, safeName);
            case net.jbock.Parameter parameter -> new SimpleParameter(method, parameter, safeName);
            default -> throw new AssertionError("unknown annotation " + annotation);
        };
    }
}
