package net.jbock.annotated;

import io.jbock.util.Either;
import io.jbock.util.Eithers;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.Name;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.jbock.util.Either.right;

final class AnnotatedMethodsBuilder {

    private final Step6 step6;
    private final List<AnnotatedParameters> repeatablePositionalParameters;

    private AnnotatedMethodsBuilder(
            Step6 step6,
            List<AnnotatedParameters> repeatablePositionalParameters) {
        this.step6 = step6;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
    }

    static Step1 builder(List<Executable> methods) {
        return new Step1(methods);
    }

    static final class Step1 {
        private final List<Executable> methods;

        Step1(List<Executable> methods) {
            this.methods = methods;
        }

        List<Executable> methods() {
            return methods;
        }

        Step2 withEnumNames(Map<Name, String> enumNames) {
            return new Step2(methods, enumNames);
        }
    }

    static final class Step2 {

        private final List<Executable> methods;
        private final Map<Name, String> enumNames;

        Step2(List<Executable> methods,
              Map<Name, String> enumNames) {
            this.methods = methods;
            this.enumNames = enumNames;
        }

        Step3 withSourceElement(SourceElement sourceElement) {
            return new Step3(this, sourceElement);
        }
    }

    static final class Step3 {

        private final Step2 step2;
        private final SourceElement sourceElement;

        Step3(Step2 step2,
              SourceElement sourceElement) {
            this.step2 = step2;
            this.sourceElement = sourceElement;
        }

        Map<Name, String> enumNames() {
            return step2.enumNames;
        }

        List<Executable> methods() {
            return step2.methods;
        }

        Step4 withAnnotatedMethods(List<AnnotatedMethod> annotatedMethods) {
            return new Step4(this, annotatedMethods);
        }
    }

    static final class Step4 {

        private final Step3 step3;
        private final List<AnnotatedMethod> annotatedMethods;

        Step4(Step3 step3, List<AnnotatedMethod> annotatedMethods) {
            this.step3 = step3;
            this.annotatedMethods = annotatedMethods;
        }

        Stream<AnnotatedMethod> annotatedMethods() {
            return annotatedMethods.stream();
        }

        Step5 withNamedOptions(List<AnnotatedOption> namedOptions) {
            return new Step5(this, namedOptions);
        }
    }

    static final class Step5 {

        private final Step4 step4;
        private final List<AnnotatedOption> namedOptions;

        Step5(Step4 step4, List<AnnotatedOption> namedOptions) {
            this.step4 = step4;
            this.namedOptions = namedOptions;
        }

        Stream<AnnotatedMethod> annotatedMethods() {
            return step4.annotatedMethods.stream();
        }

        Step6 withPositionalParameters(List<AnnotatedParameter> positionalParameters) {
            return new Step6(this, positionalParameters);
        }
    }

    static final class Step6 {

        private final Step5 step5;
        private final List<AnnotatedParameter> positionalParameters;

        Step6(Step5 step5, List<AnnotatedParameter> positionalParameters) {
            this.step5 = step5;
            this.positionalParameters = positionalParameters;
        }

        Stream<AnnotatedMethod> annotatedMethods() {
            return step5.step4.annotatedMethods.stream();
        }

        AnnotatedMethodsBuilder withRepeatablePositionalParameters(List<AnnotatedParameters> repeatablePositionalParameters) {
            return new AnnotatedMethodsBuilder(this, repeatablePositionalParameters);
        }
    }

    Either<List<ValidationFailure>, AnnotatedMethods> build() {
        return Eithers.optionalList(validateAtLeastOneParameterInSuperCommand())
                .<Either<List<ValidationFailure>, AnnotatedMethods>>map(Either::left)
                .orElseGet(() -> right(new AnnotatedMethods(
                        step6.step5.namedOptions,
                        step6.positionalParameters,
                        repeatablePositionalParameters)));
    }

    private List<ValidationFailure> validateAtLeastOneParameterInSuperCommand() {
        if (!step6.step5.step4.step3.sourceElement.isSuperCommand() ||
                !step6.positionalParameters.isEmpty()) {
            return List.of();
        }
        String message = "at least one positional parameter must be defined" +
                " when the superCommand attribute is set";
        return List.of(step6.step5.step4.step3.sourceElement.fail(message));
    }
}
