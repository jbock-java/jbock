package net.jbock.annotated;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

public class AbstractMethodsFinder {

    private final SourceElement sourceElement;

    @Inject
    public AbstractMethodsFinder(
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
    Either<List<ValidationFailure>, List<ExecutableElement>> findAbstractMethods() {
        return checkInterfaceOrSimpleClass()
                .or(this::checkNoInterfaces)
                .map(List::of)
                .<Either<List<ValidationFailure>, List<ExecutableElement>>>map(Either::left)
                .orElseGet(() -> Either.right(abstractMethods()));
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
}
