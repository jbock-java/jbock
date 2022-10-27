package net.jbock.annotated;

import java.util.List;

public final class AnnotatedMethods {

    private final AnnotatedMethodsBuilder.Step3 step3;
    private final List<ExecutableVarargsParameter> varargsParameters;

    AnnotatedMethods(
            AnnotatedMethodsBuilder.Step3 step3,
            List<ExecutableVarargsParameter> varargsParameters) {
        this.step3 = step3;
        this.varargsParameters = varargsParameters;
    }

    public List<ExecutableOption> namedOptions() {
        return step3.step2.namedOptions;
    }

    public List<ExecutableParameter> positionalParameters() {
        return step3.positionalParameters;
    }

    public List<ExecutableVarargsParameter> varargsParameters() {
        return varargsParameters;
    }
}
