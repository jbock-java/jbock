package net.jbock.annotated;

import java.util.List;

public final class AnnotatedMethods {

    private final AnnotatedMethodsBuilder.Step3 step3;
    private final List<AnnotatedVarargsParameter> varargsParameters;

    AnnotatedMethods(
            AnnotatedMethodsBuilder.Step3 step3,
            List<AnnotatedVarargsParameter> varargsParameters) {
        this.step3 = step3;
        this.varargsParameters = varargsParameters;
    }

    public List<AnnotatedOption> namedOptions() {
        return step3.step2.namedOptions;
    }

    public List<AnnotatedParameter> positionalParameters() {
        return step3.positionalParameters;
    }

    public List<AnnotatedVarargsParameter> varargsParameters() {
        return varargsParameters;
    }
}
