package net.jbock.annotated;

import java.util.List;

public final class AnnotatedMethods {

    private final List<AnnotatedOption> namedOptions;
    private final List<AnnotatedParameter> positionalParameters;
    private final List<AnnotatedParameters> repeatablePositionalParameters;

    AnnotatedMethods(
            List<AnnotatedOption> namedOptions,
            List<AnnotatedParameter> positionalParameters,
            List<AnnotatedParameters> repeatablePositionalParameters) {
        this.positionalParameters = positionalParameters;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.namedOptions = namedOptions;
    }

    public List<AnnotatedOption> namedOptions() {
        return namedOptions;
    }

    public List<AnnotatedParameter> positionalParameters() {
        return positionalParameters;
    }

    public List<AnnotatedParameters> repeatablePositionalParameters() {
        return repeatablePositionalParameters;
    }
}
