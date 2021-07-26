package net.jbock.method;

import net.jbock.annotated.AnnotatedParameters;

import java.util.OptionalInt;

public final class ParametersAnnotation extends MethodAnnotation<AnnotatedParameters> {

    private final int index;

    public ParametersAnnotation(AnnotatedParameters annotatedMethod, int index) {
        super(annotatedMethod);
        this.index = index;
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.of(index);
    }
}
