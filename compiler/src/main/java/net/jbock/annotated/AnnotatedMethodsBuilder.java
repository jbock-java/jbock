package net.jbock.annotated;

import java.util.List;
import java.util.stream.Stream;

final class AnnotatedMethodsBuilder {

    private AnnotatedMethodsBuilder() {
    }

    static Step1 builder(List<? extends Executable> annotatedMethods) {
        return new Step1(annotatedMethods);
    }

    static final class Step1 {

        private final List<? extends Executable> annotatedMethods;

        private Step1(List<? extends Executable> annotatedMethods) {
            this.annotatedMethods = annotatedMethods;
        }

        Stream<? extends Executable> annotatedMethods() {
            return annotatedMethods.stream();
        }

        Step2 withNamedOptions(List<ExecutableOption> namedOptions) {
            return new Step2(this, namedOptions);
        }
    }

    static final class Step2 {

        final Step1 step1;
        final List<ExecutableOption> namedOptions;

        private Step2(Step1 step1, List<ExecutableOption> namedOptions) {
            this.step1 = step1;
            this.namedOptions = namedOptions;
        }

        Stream<? extends Executable> annotatedMethods() {
            return step1.annotatedMethods.stream();
        }

        Step3 withPositionalParameters(List<ExecutableParameter> positionalParameters) {
            return new Step3(this, positionalParameters);
        }
    }

    static final class Step3 {

        final Step2 step2;
        final List<ExecutableParameter> positionalParameters;

        private Step3(Step2 step2, List<ExecutableParameter> positionalParameters) {
            this.step2 = step2;
            this.positionalParameters = positionalParameters;
        }

        Stream<? extends Executable> annotatedMethods() {
            return step2.step1.annotatedMethods.stream();
        }

        AnnotatedMethods withVarargsParameters(List<ExecutableVarargsParameter> varargsParameters) {
            return new AnnotatedMethods(this, varargsParameters);
        }
    }
}
