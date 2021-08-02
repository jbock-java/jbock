package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceOption;
import net.jbock.source.SourceParameter;
import net.jbock.source.SourceParameters;

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

    // sort order that puts @Parameters last
    private static final Comparator<SourceMethod<AnnotatedParameter>> POSITION_COMPARATOR =
            Comparator.comparingInt(m -> m.annotatedMethod().index());

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

    private AbstractMethods createAbstractMethods(List<SourceMethod<?>> methods) {
        List<SourceParameter> params = methods.stream()
                .map(SourceMethod::asAnnotatedParameter)
                .flatMap(Optional::stream)
                .sorted(POSITION_COMPARATOR)
                .collect(Collectors.toList());
        List<SourceParameters> repeatableParams = methods.stream()
                .map(SourceMethod::asAnnotatedParameters)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        List<SourceOption> options = methods.stream()
                .map(SourceMethod::asAnnotatedOption)
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

    private Either<List<ValidationFailure>, List<SourceMethod<?>>> validateParameterMethods(
            AllMethods sourceMethods) {
        Map<Name, EnumName> enumNames = sourceMethods.enumNames();
        return sourceMethods.abstractMethods().stream()
                .map(sourceMethod -> sourceMethodValidator.validateAnnotatedMethod(sourceMethod, enumNames))
                .collect(toValidListAll());
    }
}
