package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.Name;
import java.util.List;
import java.util.Map;

import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toValidListAll;

@ValidateScope
public class MethodsFactory {

    private final SourceElement sourceElement;
    private final AnnotatedMethodFactory sourceMethodValidator;
    private final ExecutableElementsFinder abstractMethodsFinder;

    @Inject
    MethodsFactory(
            SourceElement sourceElement,
            AnnotatedMethodFactory sourceMethodValidator,
            ExecutableElementsFinder abstractMethodsFinder) {
        this.sourceElement = sourceElement;
        this.sourceMethodValidator = sourceMethodValidator;
        this.abstractMethodsFinder = abstractMethodsFinder;
    }

    public Either<List<ValidationFailure>, AnnotatedMethods> createAnnotatedMethods() {
        return abstractMethodsFinder.findExecutableElements()
                .flatMap(this::createAnnotatedMethods)
                .map(AnnotatedMethods::create)
                .filter(methods -> optionalList(validateAtLeastOneParameterInSuperCommand(methods)));
    }

    private List<ValidationFailure> validateAtLeastOneParameterInSuperCommand(
            AnnotatedMethods abstractMethods) {
        if (!sourceElement.isSuperCommand() ||
                !abstractMethods.positionalParameters().isEmpty()) {
            return List.of();
        }
        String message = "at least one positional parameter must be defined" +
                " when the superCommand attribute is set";
        return List.of(sourceElement.fail(message));
    }

    private Either<List<ValidationFailure>, List<AnnotatedMethod>> createAnnotatedMethods(
            ExecutableElements executableElements) {
        Map<Name, EnumName> enumNames = executableElements.enumNames();
        return executableElements.executableElements().stream()
                .map(sourceMethod -> sourceMethodValidator.createAnnotatedMethod(sourceMethod, enumNames))
                .collect(toValidListAll());
    }
}
