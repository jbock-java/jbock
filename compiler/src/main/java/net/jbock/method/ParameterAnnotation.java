package net.jbock.method;

import net.jbock.annotated.AnnotatedParameter;

import java.util.OptionalInt;

public final class ParameterAnnotation extends MethodAnnotation<AnnotatedParameter> {

    public ParameterAnnotation(AnnotatedParameter annotatedMethod) {
        super(annotatedMethod);
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.of(annotatedMethod().index());
    }
}
