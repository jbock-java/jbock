package net.jbock.method;

import net.jbock.annotated.AnnotatedParameter;

import java.util.OptionalInt;

final class ParameterAnnotation extends MethodAnnotation {

    private final int index;

    ParameterAnnotation(AnnotatedParameter annotatedParameter) {
        super(annotatedParameter);
        this.index = annotatedParameter.index();
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.of(index);
    }
}
