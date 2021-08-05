package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.toOptionalList;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class ExecutableElementsFinder {

    private final SourceElement sourceElement;

    @Inject
    ExecutableElementsFinder(SourceElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    /**
     * Returns a Right-Either containing all annotated parameterless abstract
     * methods, including methods that are inherited from interfaces and
     * abstract ancestor classes.
     *
     * <p>If one of the annotated parameterless abstract methods is overridden
     * by a non-abstract method, or by another annotated method,
     * a Left-Either is returned.
     *
     * <p>If one of the unannotated parameterless abstract methods does not have
     * an annotated ancestor, a Left-Either is returned.
     *
     * @return all annotated parameterless abstract methods, including inherited,
     *         or a nonempty list of validation failures
     */
    Either<List<ValidationFailure>, AnnotatedMethodsBuilder.Step2> findExecutableElements() {
        return checkInterfaceOrSimpleClass()
                .or(this::checkNoInterfaces)
                .map(List::of)
                .<Either<List<ValidationFailure>, AnnotatedMethodsBuilder.Step2>>map(Either::left)
                .orElseGet(() -> {
                    List<ExecutableElement> allAbstract = parameterlessMethods();
                    return validateAbstractMethods(allAbstract)
                            .<Either<List<ValidationFailure>, List<Executable>>>map(Either::left)
                            .orElseGet(() -> right(allAbstract.stream()
                                    .flatMap(this::createExecutable)
                                    .collect(toList())))
                            .map(AnnotatedMethodsBuilder::builder)
                            .map(step -> step.sourceElement(sourceElement));
                });
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
                        Optional.of(sourceElement.fail("invalid superclass: expecting java.lang.Object, but found: " + superClass.getQualifiedName())));
    }

    private Optional<ValidationFailure> checkNoInterfaces() {
        List<? extends TypeMirror> interfaces = sourceElement.element().getInterfaces();
        if (!interfaces.isEmpty()) {
            return Optional.of(sourceElement.fail(
                    "invalid command class: the command class or interface may not implement or extend any interfaces," +
                            " but found: " + interfaces.stream()
                            .map(AS_DECLARED::visit)
                            .flatMap(Optional::stream)
                            .map(DeclaredType::asElement)
                            .map(Element::getSimpleName)
                            .map(Name::toString)
                            .collect(toList())));
        }
        return Optional.empty();
    }

    private List<ExecutableElement> parameterlessMethods() {
        return ElementFilter.methodsIn(sourceElement.element().getEnclosedElements()).stream()
                .filter(m -> m.getParameters().isEmpty())
                .filter(m -> m.getModifiers().contains(ABSTRACT))
                .collect(toList());
    }

    private Optional<List<ValidationFailure>> validateAbstractMethods(
            List<ExecutableElement> allAbstract) {
        return allAbstract.stream()
                .filter(m -> !hasAnnotation(m))
                .map(this::missingAnnotationError)
                .collect(toOptionalList());
    }

    private boolean hasAnnotation(ExecutableElement method) {
        return methodLevelAnnotations().stream()
                .anyMatch(a -> method.getAnnotation(a) != null);
    }

    private Stream<Executable> createExecutable(ExecutableElement method) {
        return methodLevelAnnotations().stream()
                .flatMap(a -> method.getAnnotation(a) != null ?
                        Stream.of(Executable.create(method, method.getAnnotation(a))) :
                        Stream.empty());
    }

    private ValidationFailure missingAnnotationError(ExecutableElement m) {
        String message = "add one of these annotations: " + methodLevelAnnotations().stream()
                .map(Class::getSimpleName).collect(toList());
        message = message + " to method '" +
                m.getEnclosingElement().getSimpleName() +
                "." + m.getSimpleName() + "'";
        return new ValidationFailure(message, m);
    }
}
