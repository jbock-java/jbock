package net.jbock.validate;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.parameter.SourceMethod;

import java.util.List;

class AbstractMethods {

    private final List<SourceMethod<?>> positionalParameters;
    private final List<SourceMethod<AnnotatedOption>> namedOptions;

    AbstractMethods(
            List<SourceMethod<?>> positionalParameters,
            List<SourceMethod<AnnotatedOption>> namedOptions) {
        this.positionalParameters = positionalParameters;
        this.namedOptions = namedOptions;
    }

    List<SourceMethod<?>> positionalParameters() {
        return positionalParameters;
    }

    List<SourceMethod<AnnotatedOption>> namedOptions() {
        return namedOptions;
    }
}
