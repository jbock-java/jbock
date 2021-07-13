package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.Parameters;
import net.jbock.common.ValidationFailure;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ValidateScope
public class MethodsFactory {

    // sort order that puts @Parameters last (they don't have an index)
    private static final Comparator<SourceMethod> POSITION_COMPARATOR =
            Comparator.comparingInt(m -> m.index().orElse(Integer.MAX_VALUE));

    private final SourceElement sourceElement;
    private final ParameterMethodValidator parameterMethodValidator;
    private final AbstractMethodsFinder abstractMethodsFinder;

    @Inject
    MethodsFactory(
            SourceElement sourceElement,
            ParameterMethodValidator parameterMethodValidator,
            AbstractMethodsFinder abstractMethodsFinder) {
        this.sourceElement = sourceElement;
        this.parameterMethodValidator = parameterMethodValidator;
        this.abstractMethodsFinder = abstractMethodsFinder;
    }

    /**
     * find unimplemented abstract methods in {@code sourceElement} and its ancestors
     */
    Either<List<ValidationFailure>, AbstractMethods> findAbstractMethods() {
        return abstractMethodsFinder.findAbstractMethods()
                .filter(this::validateParameterMethods)
                .filter(this::detectInheritanceCollision)
                .map(this::createSourceMethods)
                .filter(this::validateDuplicateParametersAnnotation)
                .map(this::createAbstractMethods)
                .filter(this::validateAtLeastOneParameterInSuperCommand);
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> detectInheritanceCollision(
            List<ExecutableElement> methods) {
        Map<Name, List<ExecutableElement>> map = methods.stream()
                .collect(Collectors.groupingBy(ExecutableElement::getSimpleName));
        for (ExecutableElement method : methods) {
            if (map.get(method.getSimpleName()).size() >= 2) {
                ValidationFailure f = new ValidationFailure("inheritance collision", method);
                return Optional.of(List.of(f));
            }
        }
        return Optional.empty();
    }

    private AbstractMethods createAbstractMethods(
            List<SourceMethod> methods) {
        List<SourceMethod> params = methods.stream()
                .filter(m -> m.style().isPositional())
                .sorted(POSITION_COMPARATOR)
                .collect(Collectors.toUnmodifiableList());
        List<SourceMethod> options = methods.stream()
                .filter(m -> !m.style().isPositional())
                .collect(Collectors.toUnmodifiableList());
        return new AbstractMethods(params, options);
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateAtLeastOneParameterInSuperCommand(
            AbstractMethods abstractMethods) {
        if (!sourceElement.isSuperCommand() ||
                !abstractMethods.positionalParameters().isEmpty()) {
            return Optional.empty();
        }
        String message = "at least one positional parameter must be defined" +
                " when the superCommand attribute is set";
        return Optional.of(List.of(sourceElement.fail(message)));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateDuplicateParametersAnnotation(
            List<SourceMethod> sourceMethods) {
        List<SourceMethod> parametersMethods = sourceMethods.stream()
                .filter(m -> m.style() == ParameterStyle.PARAMETERS)
                .collect(Collectors.toUnmodifiableList());
        if (parametersMethods.size() >= 2) {
            String message = "duplicate @" + Parameters.class.getSimpleName() + " annotation";
            return Optional.of(List.of(sourceMethods.get(1).fail(message)));
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateParameterMethods(
            List<ExecutableElement> sourceMethods) {
        List<ValidationFailure> failures = new ArrayList<>();
        for (ExecutableElement sourceMethod : sourceMethods) {
            parameterMethodValidator.validateParameterMethod(sourceMethod)
                    .map(msg -> new ValidationFailure(msg, sourceMethod))
                    .ifPresent(failures::add);
        }
        if (!failures.isEmpty()) {
            return Optional.of(failures);
        }
        return Optional.empty();
    }

    private List<SourceMethod> createSourceMethods(List<ExecutableElement> methods) {
        return methods.stream()
                .map(SourceMethod::create)
                .collect(Collectors.toUnmodifiableList());
    }
}
