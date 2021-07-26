package net.jbock.method;

import net.jbock.annotated.AnnotatedMethod;

import java.util.OptionalInt;

final class ParameterAnnotation extends MethodAnnotation {

    private final int index;

    ParameterAnnotation(
            AnnotatedMethod annotatedMethod,
            int index) {
        super(annotatedMethod);
        this.index = index;
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.of(index);
    }
}
