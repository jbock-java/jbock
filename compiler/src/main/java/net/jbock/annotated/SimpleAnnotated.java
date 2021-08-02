package net.jbock.annotated;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.lang.annotation.Annotation;

final class SimpleAnnotated {

    private final ExecutableElement method;
    private final Annotation annotation;

    private SimpleAnnotated(ExecutableElement method, Annotation annotation) {
        this.method = method;
        this.annotation = annotation;
    }

    static SimpleAnnotated create(ExecutableElement method, Annotation annotation) {
        return new SimpleAnnotated(method, annotation);
    }

    ExecutableElement method() {
        return method;
    }

    Annotation annotation() {
        return annotation;
    }

    Name getSimpleName() {
        return method.getSimpleName();
    }
}
