package net.jbock.validate;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;

import java.util.List;

class AbstractMethods {

    private final List<AnnotatedParameter> positionalParameters;
    private final List<AnnotatedParameters> repeatablePositionalParameters;
    private final List<AnnotatedOption> namedOptions;

    AbstractMethods(
            List<AnnotatedParameter> positionalParameters,
            List<AnnotatedParameters> repeatablePositionalParameters,
            List<AnnotatedOption> namedOptions) {
        this.positionalParameters = positionalParameters;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.namedOptions = namedOptions;
    }

    List<AnnotatedParameter> positionalParameters() {
        return positionalParameters;
    }

    List<AnnotatedParameters> repeatablePositionalParameters() {
        return repeatablePositionalParameters;
    }

    List<AnnotatedOption> namedOptions() {
        return namedOptions;
    }
}
