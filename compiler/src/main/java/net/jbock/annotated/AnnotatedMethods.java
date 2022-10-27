package net.jbock.annotated;

import java.util.List;

public final class AnnotatedMethods {

    private final AnnotatedMethodsBuilder.Step3 step3;
    private final List<VarargsParameter> varargsParameters;

    AnnotatedMethods(
            AnnotatedMethodsBuilder.Step3 step3,
            List<VarargsParameter> varargsParameters) {
        this.step3 = step3;
        this.varargsParameters = varargsParameters;
    }

    public List<Option> namedOptions() {
        return step3.step2.namedOptions;
    }

    public List<Parameter> positionalParameters() {
        return step3.positionalParameters;
    }

    public List<VarargsParameter> varargsParameters() {
        return varargsParameters;
    }
}
