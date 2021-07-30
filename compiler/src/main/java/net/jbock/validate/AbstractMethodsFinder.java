package net.jbock.validate;

import io.jbock.util.Either;
import io.jbock.util.Eithers;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class AbstractMethodsFinder {

    private final Types types;
    private final SourceElement sourceElement;

    @Inject
    AbstractMethodsFinder(
            Types types,
            SourceElement sourceElement) {
        this.types = types;
        this.sourceElement = sourceElement;
    }

    Either<List<ValidationFailure>, List<ExecutableElement>> findAbstractMethods() {
        Map<Boolean, List<ExecutableElement>> partitions = findMethodsIn(sourceElement.element().asType()).stream()
                .collect(partitioningBy(m -> m.getModifiers().contains(ABSTRACT)));
        Map<Name, List<ExecutableElement>> nonAbstractMethods = partitions.get(false)
                .stream()
                .collect(groupingBy(ExecutableElement::getSimpleName));
        return findRelevantAbstractMethods(partitions.get(true), nonAbstractMethods);
    }

    private List<ExecutableElement> findMethodsIn(TypeMirror mirror) {
        Map<Name, List<ExecutableElement>> methodsInInterfaces = findMethodsInInterfaces(mirror);
        List<ExecutableElement> acc = new ArrayList<>();
        methodsInInterfaces.values().forEach(acc::addAll);
        while (true) {
            Optional<TypeElement> element = asAbstractTypeElement(mirror);
            if (element.isEmpty()) {
                return acc;
            }
            TypeElement el = element.orElseThrow();
            List<ExecutableElement> methods = methodsIn(el.getEnclosedElements());
            acc.addAll(methods);
            mirror = el.getSuperclass();
        }
    }

    private Optional<TypeElement> asAbstractTypeElement(TypeMirror mirror) {
        return AS_DECLARED.visit(mirror)
                .map(DeclaredType::asElement)
                .flatMap(AS_TYPE_ELEMENT::visit)
                // interfaces are handled separately
                .filter(typeElement -> typeElement.getKind() != INTERFACE)
                // not abstract -> no relevant methods
                .filter(typeElement -> typeElement.getModifiers().contains(ABSTRACT));
    }

    private Map<Name, List<ExecutableElement>> findMethodsInInterfaces(TypeMirror mirror) {
        return AS_DECLARED.visit(mirror)
                .map(DeclaredType::asElement)
                .flatMap(AS_TYPE_ELEMENT::visit)
                .map(typeElement -> {
                    List<ExecutableElement> methods = typeElement.getKind() == INTERFACE ?
                            methodsIn(typeElement.getEnclosedElements()) :
                            List.of();
                    Map<Name, List<ExecutableElement>> acc = new HashMap<>();
                    acc.put(typeElement.getQualifiedName(), methods);
                    for (TypeMirror superInterface : typeElement.getInterfaces()) {
                        acc.putAll(findMethodsInInterfaces(superInterface)); // recursion
                    }
                    return acc;
                }).orElse(Map.of());
    }

    /**
     * Returns only those abstract methods that are not overridden
     * by a non-abstract method.
     * An overridden method must not have a jbock annotation.
     *
     * @param allAbstract the set of all abstract methods in the source elements hierarchy
     * @return the n
     */
    private Either<List<ValidationFailure>, List<ExecutableElement>> findRelevantAbstractMethods(
            List<ExecutableElement> allAbstract,
            Map<Name, List<ExecutableElement>> nonAbstract) {
        Map<Boolean, List<ExecutableElement>> partition = allAbstract.stream()
                .collect(partitioningBy(m -> nonAbstract.getOrDefault(m.getSimpleName(), List.of()).stream()
                        .anyMatch(method -> isSameSignature(method, m))));
        return partition.get(true).stream()
                .filter(this::hasAnnotation)
                .map(m -> new ValidationFailure("annotated method is overridden", m))
                .collect(Eithers.toOptionalList())
                .<Either<List<ValidationFailure>, List<ExecutableElement>>>map(Either::left)
                .orElseGet(() -> right(partition.get(false)));
    }

    private boolean isSameSignature(ExecutableElement m1, ExecutableElement m2) {
        if (m1.getParameters().size() != m2.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < m1.getParameters().size(); i++) {
            VariableElement p1 = m1.getParameters().get(i);
            VariableElement p2 = m2.getParameters().get(i);
            if (!types.isSameType(p1.asType(), p2.asType())) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAnnotation(ExecutableElement overridden) {
        return methodLevelAnnotations().stream()
                .anyMatch(a -> overridden.getAnnotation(a) != null);
    }
}
