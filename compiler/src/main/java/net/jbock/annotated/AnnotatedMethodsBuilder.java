package net.jbock.annotated;

import io.jbock.util.Either;
import io.jbock.util.Eithers;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.Name;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.jbock.util.Either.right;
import static java.util.stream.Collectors.toList;

final class AnnotatedMethodsBuilder {

    private static final Comparator<AnnotatedParameter> INDEX_COMPARATOR =
            Comparator.comparingInt(AnnotatedParameter::index);

    private final Step5 step5;
    private final List<AnnotatedParameters> repeatablePositionalParameters;

    private AnnotatedMethodsBuilder(
            Step5 step5,
            List<AnnotatedParameters> repeatablePositionalParameters) {
        this.step5 = step5;
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

        Step2 sourceElement(SourceElement sourceElement) {
            return new Step2(methods, createEnumNames(methods), sourceElement);
        }
    }

    private static Map<Name, EnumName> createEnumNames(List<Executable> methods) {
        Set<EnumName> names = new HashSet<>();
        Map<Name, EnumName> result = new HashMap<>();
        for (Executable method : methods) {
            EnumName enumName = EnumName.create(method.simpleName().toString());
            while (names.contains(enumName)) {
                enumName = enumName.makeLonger();
            }
            names.add(enumName);
            result.put(method.simpleName(), enumName);
        }
        return result;
    }

    static final class Step2 {

        private final List<Executable> methods;
        private final Map<Name, EnumName> enumNames;
        private final SourceElement sourceElement;

        Step2(List<Executable> methods,
              Map<Name, EnumName> enumNames,
              SourceElement sourceElement) {
            this.methods = methods;
            this.enumNames = enumNames;
            this.sourceElement = sourceElement;
        }

        Map<Name, EnumName> enumNames() {
            return enumNames;
        }

        List<Executable> methods() {
            return methods;
        }

        Step3 withAnnotatedMethods(List<AnnotatedMethod> annotatedMethods) {
            return new Step3(this, annotatedMethods);
        }
    }

    static final class Step3 {

        private final Step2 step2;
        private final List<AnnotatedMethod> annotatedMethods;

        Step3(Step2 step2, List<AnnotatedMethod> annotatedMethods) {
            this.step2 = step2;
            this.annotatedMethods = annotatedMethods;
        }

        Step4 withNamedOptions() {
            List<AnnotatedOption> namedOptions = annotatedMethods.stream()
                    .flatMap(AnnotatedMethod::asAnnotatedOption)
                    .collect(toList());
            return new Step4(this, namedOptions);
        }
    }

    static final class Step4 {

        private final Step3 step3;
        private final List<AnnotatedOption> namedOptions;

        Step4(Step3 step3, List<AnnotatedOption> namedOptions) {
            this.step3 = step3;
            this.namedOptions = namedOptions;
        }

        Step5 withPositionalParameters() {
            List<AnnotatedParameter> positionalParameters = step3.annotatedMethods.stream()
                    .flatMap(AnnotatedMethod::asAnnotatedParameter)
                    .sorted(INDEX_COMPARATOR)
                    .collect(toList());
            return new Step5(this, positionalParameters);
        }
    }

    static final class Step5 {

        private final Step4 step4;
        private final List<AnnotatedParameter> positionalParameters;

        Step5(Step4 step4, List<AnnotatedParameter> positionalParameters) {
            this.step4 = step4;
            this.positionalParameters = positionalParameters;
        }

        AnnotatedMethodsBuilder withRepeatablePositionalParameters() {
            List<AnnotatedParameters> repeatablePositionalParameters = step4.step3.annotatedMethods.stream()
                    .flatMap(AnnotatedMethod::asAnnotatedParameters)
                    .collect(toList());
            return new AnnotatedMethodsBuilder(this, repeatablePositionalParameters);
        }
    }

    Either<List<ValidationFailure>, AnnotatedMethods> build() {
        return Eithers.optionalList(validateAtLeastOneParameterInSuperCommand())
                .<Either<List<ValidationFailure>, AnnotatedMethods>>map(Either::left)
                .orElseGet(() -> right(new AnnotatedMethods(
                        step5.step4.namedOptions,
                        step5.positionalParameters,
                        repeatablePositionalParameters)));
    }

    private List<ValidationFailure> validateAtLeastOneParameterInSuperCommand() {
        if (!step5.step4.step3.step2.sourceElement.isSuperCommand() ||
                !step5.positionalParameters.isEmpty()) {
            return List.of();
        }
        String message = "at least one positional parameter must be defined" +
                " when the superCommand attribute is set";
        return List.of(step5.step4.step3.step2.sourceElement.fail(message));
    }
}
