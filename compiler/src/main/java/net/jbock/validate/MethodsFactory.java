package net.jbock.validate;

import io.jbock.util.Either;
import io.jbock.util.Eithers;
import net.jbock.Parameters;
import net.jbock.common.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toOptionalList;

@ValidateScope
public class MethodsFactory {

    // sort order that puts @Parameters last
    private static final Comparator<SourceMethod> POSITION_COMPARATOR =
            Comparator.comparingInt(m -> m.index().orElse(Integer.MAX_VALUE));

    private final SourceElement sourceElement;
    private final SourceMethodValidator sourceMethodValidator;
    private final AbstractMethodsFinder abstractMethodsFinder;

    @Inject
    MethodsFactory(
            SourceElement sourceElement,
            SourceMethodValidator sourceMethodValidator,
            AbstractMethodsFinder abstractMethodsFinder) {
        this.sourceElement = sourceElement;
        this.sourceMethodValidator = sourceMethodValidator;
        this.abstractMethodsFinder = abstractMethodsFinder;
    }

    /**
     * find unimplemented abstract methods in {@code sourceElement} and its ancestors
     */
    Either<List<ValidationFailure>, AbstractMethods> findAbstractMethods() {
        return abstractMethodsFinder.findAbstractMethods()
                .flatMap(this::validateParameterMethods)
                .filter(this::detectInheritanceCollision)
                .map(this::createSourceMethods)
                .filter(this::validateDuplicateParametersAnnotation)
                .map(this::createAbstractMethods)
                .filter(methods -> optionalList(validateAtLeastOneParameterInSuperCommand(methods)));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> detectInheritanceCollision(
            List<AnnotatedMethod> methods) {
        Map<Name, List<ExecutableElement>> map = methods.stream()
                .map(AnnotatedMethod::sourceMethod)
                .collect(Collectors.groupingBy(ExecutableElement::getSimpleName));
        return methods.stream()
                .map(AnnotatedMethod::sourceMethod)
                .filter(method -> map.get(method.getSimpleName()).size() >= 2)
                .map(method -> new ValidationFailure("inheritance collision", method))
                .collect(toOptionalList());
    }

    private AbstractMethods createAbstractMethods(List<SourceMethod> methods) {
        List<SourceMethod> params = methods.stream()
                .filter(SourceMethod::isPositional)
                .sorted(POSITION_COMPARATOR)
                .collect(Collectors.toUnmodifiableList());
        List<SourceMethod> options = methods.stream()
                .filter(m -> !m.isPositional())
                .collect(Collectors.toUnmodifiableList());
        return new AbstractMethods(params, options);
    }

    private List<ValidationFailure> validateAtLeastOneParameterInSuperCommand(
            AbstractMethods abstractMethods) {
        if (!sourceElement.isSuperCommand() ||
                !abstractMethods.positionalParameters().isEmpty()) {
            return List.of();
        }
        String message = "at least one positional parameter must be defined" +
                " when the superCommand attribute is set";
        return List.of(sourceElement.fail(message));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateDuplicateParametersAnnotation(
            List<SourceMethod> sourceMethods) {
        List<SourceMethod> parametersMethods = sourceMethods.stream()
                .filter(SourceMethod::isParameters)
                .collect(Collectors.toUnmodifiableList());
        List<ValidationFailure> failures = new ArrayList<>();
        if (parametersMethods.size() >= 2) {
            String message = "duplicate @" + Parameters.class.getSimpleName() + " annotation";
            failures.add(sourceMethods.get(1).fail(message));
        }
        return optionalList(failures);
    }

    private Either<List<ValidationFailure>, List<AnnotatedMethod>> validateParameterMethods(
            List<ExecutableElement> sourceMethods) {
        return sourceMethods.stream()
                .map(sourceMethodValidator::validateSourceMethod)
                .collect(Eithers.toValidListAll());
    }

    private List<SourceMethod> createSourceMethods(List<AnnotatedMethod> methods) {
        int numberOfParameters = Math.toIntExact(methods.stream()
                .filter(AnnotatedMethod::isParameter)
                .count());
        Set<EnumName> names = new HashSet<>();
        List<SourceMethod> result = new ArrayList<>();
        for (AnnotatedMethod method : methods) {
            EnumName name = EnumName.create(method.sourceMethod().getSimpleName().toString());
            while (names.contains(name)) {
                name = name.makeLonger();
            }
            names.add(name);
            result.add(SourceMethod.create(method, name, numberOfParameters));
        }
        return result;
    }
}
