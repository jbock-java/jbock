package net.jbock.method;

import net.jbock.annotated.AnnotatedOption;

import java.util.OptionalInt;

public final class OptionAnnotation extends MethodAnnotation<AnnotatedOption> {

    public OptionAnnotation(AnnotatedOption annotatedOption) {
        super(annotatedOption);
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.empty();
    }
}
