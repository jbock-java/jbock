package net.jbock.annotated;

import java.util.List;
import java.util.stream.Stream;

final class AnnotatedMethodsBuilder {

    private AnnotatedMethodsBuilder() {
    }

    static Step1 builder(List<AnnotatedMethod> annotatedMethods) {
        return new Step1(annotatedMethods);
    }

    static final class Step1 {

        private final List<AnnotatedMethod> annotatedMethods;

        private Step1(List<AnnotatedMethod> annotatedMethods) {
            this.annotatedMethods = annotatedMethods;
        }

        Stream<AnnotatedMethod> annotatedMethods() {
            return annotatedMethods.stream();
        }

        Step2 withNamedOptions(List<AnnotatedOption> namedOptions) {
            return new Step2(this, namedOptions);
        }
    }

    static final class Step2 {

        final Step1 step1;
        final List<AnnotatedOption> namedOptions;

        private Step2(Step1 step1, List<AnnotatedOption> namedOptions) {
            this.step1 = step1;
            this.namedOptions = namedOptions;
        }

        Stream<AnnotatedMethod> annotatedMethods() {
            return step1.annotatedMethods.stream();
        }

        Step3 withPositionalParameters(List<AnnotatedParameter> positionalParameters) {
            return new Step3(this, positionalParameters);
        }
    }

    static final class Step3 {

        final Step2 step2;
        final List<AnnotatedParameter> positionalParameters;

        private Step3(Step2 step2, List<AnnotatedParameter> positionalParameters) {
            this.step2 = step2;
            this.positionalParameters = positionalParameters;
        }

        Stream<AnnotatedMethod> annotatedMethods() {
            return step2.step1.annotatedMethods.stream();
        }

        AnnotatedMethods withVarargsParameters(List<AnnotatedVarargsParameter> varargsParameters) {
            return new AnnotatedMethods(this, varargsParameters);
        }
    }
}
