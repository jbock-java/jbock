package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.Name;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toValidListAll;

@ValidateScope
public class MethodsFactory {

    private static final Comparator<AnnotatedParameter> INDEX_COMPARATOR =
            Comparator.comparingInt(AnnotatedParameter::index);

    private final SourceElement sourceElement;
    private final AnnotatedMethodValidator sourceMethodValidator;
    private final AbstractMethodsFinder abstractMethodsFinder;

    @Inject
    MethodsFactory(
            SourceElement sourceElement,
            AnnotatedMethodValidator sourceMethodValidator,
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
                .map(this::createAbstractMethods)
                .filter(methods -> optionalList(validateAtLeastOneParameterInSuperCommand(methods)));
    }

    private AbstractMethods createAbstractMethods(List<AnnotatedMethod> methods) {
        List<AnnotatedParameter> params = methods.stream()
                .map(AnnotatedMethod::asAnnotatedParameter)
                .flatMap(Optional::stream)
                .sorted(INDEX_COMPARATOR)
                .collect(Collectors.toList());
        List<AnnotatedParameters> repeatableParams = methods.stream()
                .map(AnnotatedMethod::asAnnotatedParameters)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        List<AnnotatedOption> options = methods.stream()
                .map(AnnotatedMethod::asAnnotatedOption)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        return new AbstractMethods(params, repeatableParams, options);
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

    private Either<List<ValidationFailure>, List<AnnotatedMethod>> validateParameterMethods(
            AllMethods sourceMethods) {
        Map<Name, EnumName> enumNames = sourceMethods.enumNames();
        return sourceMethods.abstractMethods().stream()
                .map(sourceMethod -> sourceMethodValidator.validateAnnotatedMethod(sourceMethod, enumNames))
                .collect(toValidListAll());
    }
}
