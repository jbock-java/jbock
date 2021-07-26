package net.jbock.method;

import net.jbock.annotated.AnnotatedOption;

import java.util.OptionalInt;

final class OptionAnnotation extends MethodAnnotation {

    OptionAnnotation(AnnotatedOption annotatedOption) {
        super(annotatedOption);
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.empty();
    }
}
