package net.jbock.annotated;

import java.util.List;
import java.util.stream.Stream;

final class AnnotatedMethodsBuilder {

    private AnnotatedMethodsBuilder() {
    }

    static Step1 builder(List<? extends Item> annotatedMethods) {
        return new Step1(annotatedMethods);
    }

    static final class Step1 {

        private final List<? extends Item> annotatedMethods;

        private Step1(List<? extends Item> annotatedMethods) {
            this.annotatedMethods = annotatedMethods;
        }

        Stream<? extends Item> annotatedMethods() {
            return annotatedMethods.stream();
        }

        Step2 withNamedOptions(List<Option> namedOptions) {
            return new Step2(this, namedOptions);
        }
    }

    static final class Step2 {

        final Step1 step1;
        final List<Option> namedOptions;

        private Step2(Step1 step1, List<Option> namedOptions) {
            this.step1 = step1;
            this.namedOptions = namedOptions;
        }

        Stream<? extends Item> annotatedMethods() {
            return step1.annotatedMethods.stream();
        }

        Step3 withPositionalParameters(List<Parameter> positionalParameters) {
            return new Step3(this, positionalParameters);
        }
    }

    static final class Step3 {

        final Step2 step2;
        final List<Parameter> positionalParameters;

        private Step3(Step2 step2, List<Parameter> positionalParameters) {
            this.step2 = step2;
            this.positionalParameters = positionalParameters;
        }

        Stream<? extends Item> annotatedMethods() {
            return step2.step1.annotatedMethods.stream();
        }

        AnnotatedMethods withVarargsParameters(List<VarargsParameter> varargsParameters) {
            return new AnnotatedMethods(this, varargsParameters);
        }
    }
}
