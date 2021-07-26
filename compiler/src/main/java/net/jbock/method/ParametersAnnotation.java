package net.jbock.method;

import net.jbock.annotated.AnnotatedParameters;

import java.util.OptionalInt;

final class ParametersAnnotation extends MethodAnnotation {

    private final int numberOfParameters;

    ParametersAnnotation(
            AnnotatedParameters annotatedParameters,
            int numberOfParameters) {
        super(annotatedParameters);
        this.numberOfParameters = numberOfParameters;
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.of(numberOfParameters);
    }
}
