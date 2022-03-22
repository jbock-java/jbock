package net.jbock.annotated;

import java.util.List;

public final class AnnotatedMethods {

    private final AnnotatedMethodsBuilder.Step3 step3;
    private final List<AnnotatedVarargsParameter> repeatablePositionalParameters;

    AnnotatedMethods(
            AnnotatedMethodsBuilder.Step3 step3,
            List<AnnotatedVarargsParameter> repeatablePositionalParameters) {
        this.step3 = step3;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
    }

    public List<AnnotatedOption> namedOptions() {
        return step3.step2.namedOptions;
    }

    public List<AnnotatedParameter> positionalParameters() {
        return step3.positionalParameters;
    }

    public List<AnnotatedVarargsParameter> repeatablePositionalParameters() {
        return repeatablePositionalParameters;
    }
}
