package net.jbock.annotated;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.common.SnakeName;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static io.jbock.util.Either.left;
import static io.jbock.util.Eithers.allFailures;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class ExecutableElementsFinder {

    private final SourceElement sourceElement;

    @Inject
    ExecutableElementsFinder(
            SourceElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    /**
     * Returns a Right-Either containing all annotated parameterless
     * abstract methods.
     *
     * <p>If one of the abstract methods is not annotated,
     * or not parameterless, a Left-Either is returned.
     *
     * @return all annotated parameterless abstract methods,
     *         or a nonempty list of validation failures
     */
    Either<List<ValidationFailure>, List<Executable>> findExecutableElements() {
        return checkInterfaceOrSimpleClass()
                .or(this::checkNoInterfaces)
                .map(List::of)
                .<Either<List<ValidationFailure>, List<Executable>>>map(Either::left)
                .orElseGet(() -> new ParameterlessAbstractValidator(abstractMethods()).validParameterlessAbstract());
    }
    
    private static class ParameterlessAbstractValidator {

        final List<ExecutableElement> abstractMethods;
        final UniqueNameSet uniqueNameSet = new UniqueNameSet();

        ParameterlessAbstractValidator(List<ExecutableElement> abstractMethods) {
            this.abstractMethods = abstractMethods;
        }

        Either<List<ValidationFailure>, List<Executable>> validParameterlessAbstract() {
            return abstractMethods.stream()
                    .map(this::validateAbstractMethod)
                    .collect(allFailures());
        }

        Either<ValidationFailure, Executable> validateAbstractMethod(
                ExecutableElement method) {
            String enumName = enumNameFor(method.getSimpleName());
            return getMethodAnnotation(method)
                    .map(a -> Executable.create(method, a, enumName))
                    .filter(ExecutableElementsFinder::validateParameterless);
        }

        String enumNameFor(Name sourceMethodName) {
            String enumName = "_".contentEquals(sourceMethodName) ?
                    "_1" : // avoid potential keyword issue
                    SnakeName.create(sourceMethodName).snake('_').toUpperCase(Locale.ROOT);
            return uniqueNameSet.getUniqueName(enumName);
        }
    }

    private Optional<ValidationFailure> checkInterfaceOrSimpleClass() {
        if (sourceElement.isInterface()) {
            return Optional.empty();
        }
        return AS_DECLARED.visit(sourceElement.element().getSuperclass())
                .map(DeclaredType::asElement)
                .flatMap(AS_TYPE_ELEMENT::visit)
                .flatMap(superClass -> superClass.getSuperclass().getKind() == TypeKind.NONE ?
                        Optional.empty() :
                        Optional.of(sourceElement.fail(
                                "invalid superclass: expecting java.lang.Object, but found: "
                                        + superClass.getQualifiedName())));
    }

    private Optional<ValidationFailure> checkNoInterfaces() {
        List<? extends TypeMirror> interfaces = sourceElement.element().getInterfaces();
        if (interfaces.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(sourceElement.fail(
                "invalid command class: the command class or interface may not implement or extend any interfaces," +
                        " but found: " + interfaces.stream()
                        .map(AS_DECLARED::visit)
                        .flatMap(Optional::stream)
                        .map(DeclaredType::asElement)
                        .map(Element::getSimpleName)
                        .collect(toList())));
    }

    private List<ExecutableElement> abstractMethods() {
        return ElementFilter.methodsIn(sourceElement.element().getEnclosedElements()).stream()
                .filter(m -> m.getModifiers().contains(ABSTRACT))
                .collect(toList());
    }
    
    private static Either<ValidationFailure, Annotation> getMethodAnnotation(
            ExecutableElement method) {
        return methodLevelAnnotations().stream()
                .map(method::getAnnotation)
                .filter(Objects::nonNull)
                .findFirst()
                .<Either<ValidationFailure, Annotation>>map(Either::right)
                .orElseGet(() -> left(missingAnnotationError(method)));
    }

    private static Optional<ValidationFailure> validateParameterless(
            Executable method) {
        if (method.method().getParameters().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(method.fail("invalid method parameters: abstract method '" +
                method.method().getSimpleName() +
                "' may not have any parameters, but found: " +
                method.method().getParameters().stream()
                        .map(VariableElement::getSimpleName)
                        .map(Name::toString)
                        .collect(toList())));
    }

    private static ValidationFailure missingAnnotationError(
            ExecutableElement method) {
        String message = "missing annotation: add one of these annotations: " + methodLevelAnnotations().stream()
                .map(Class::getSimpleName).collect(toList());
        message = message + " to method '" + method.getSimpleName() + "'";
        return new ValidationFailure(message, method);
    }
}
