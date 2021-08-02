package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.toOptionalList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class ExecutableElementsFinder {

    private final SourceElement sourceElement;
    private final Util util;

    @Inject
    ExecutableElementsFinder(
            SourceElement sourceElement,
            Util util) {
        this.sourceElement = sourceElement;
        this.util = util;
    }

    /**
     * Returns a Right-Either containing all parameterless abstract methods,
     * including inherited abstract methods.
     * If one of the parameterless abstract methods is overridden,
     * a Left either is returned.
     *
     * @return all parameterless abstract methods, including inherited,
     *         or a nonempty list of validation failures
     */
    Either<List<ValidationFailure>, ExecutableElements> findExecutableElements() {
        List<ExecutableElement> methods = findMethodsIn(sourceElement.element().asType());
        Map<Boolean, List<ExecutableElement>> partitions = methods.stream()
                .collect(partitioningBy(m -> m.getModifiers().contains(ABSTRACT)));
        Set<Name> nonAbstractNames = partitions.get(false).stream()
                .map(ExecutableElement::getSimpleName)
                .collect(Collectors.toSet());
        Map<Name, List<ExecutableElement>> allAbstractGrouped = partitions.get(true).stream()
                .collect(groupingBy(ExecutableElement::getSimpleName));
        return findRelevantAbstractMethods(partitions.get(true), allAbstractGrouped, nonAbstractNames);
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
            List<ExecutableElement> methods = parameterlessMethodsIn(el.getEnclosedElements());
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
                            parameterlessMethodsIn(typeElement.getEnclosedElements()) :
                            List.of();
                    Map<Name, List<ExecutableElement>> acc = new HashMap<>();
                    acc.put(typeElement.getQualifiedName(), methods);
                    for (TypeMirror superInterface : typeElement.getInterfaces()) {
                        acc.putAll(findMethodsInInterfaces(superInterface)); // recursion
                    }
                    return acc;
                }).orElse(Map.of());
    }

    private List<ExecutableElement> parameterlessMethodsIn(List<? extends Element> elements) {
        return ElementFilter.methodsIn(elements).stream()
                .filter(m -> m.getParameters().isEmpty())
                .collect(toList());
    }

    private Either<List<ValidationFailure>, ExecutableElements> findRelevantAbstractMethods(
            List<ExecutableElement> allAbstract,
            Map<Name, List<ExecutableElement>> allAbstractNameCount,
            Set<Name> nonAbstractNames) {
        List<ValidationFailure> missingAnnotationFailures = allAbstract.stream()
                .filter(m -> !nonAbstractNames.contains(m.getSimpleName())
                        && missingAnnotation(allAbstractNameCount.get(m.getSimpleName())))
                .map(m -> new ValidationFailure(util.missingAnnotationError(), m))
                .collect(toList());
        if (!missingAnnotationFailures.isEmpty()) {
            return left(missingAnnotationFailures);
        }
        return allAbstract.stream()
                .filter(this::hasAnnotation)
                .filter(m -> nonAbstractNames.contains(m.getSimpleName())
                        || badOverride(allAbstractNameCount.get(m.getSimpleName())))
                .map(m -> new ValidationFailure("annotated method is overridden", m))
                .collect(toOptionalList())
                .<Either<List<ValidationFailure>, List<ExecutableElement>>>map(Either::left)
                .orElseGet(() -> right(allAbstract.stream()
                        .filter(m -> !nonAbstractNames.contains(m.getSimpleName()))
                        .filter(this::hasAnnotation)
                        .collect(toList())))
                .map(ExecutableElements::create);
    }

    private boolean badOverride(
            List<ExecutableElement> overrides) {
        return overrides.stream()
                .filter(this::hasAnnotation)
                .count() >= 2;
    }

    private boolean missingAnnotation(List<ExecutableElement> overrides) {
        return overrides.stream()
                .filter(this::hasAnnotation)
                .findAny()
                .isEmpty();
    }

    private boolean hasAnnotation(ExecutableElement overridden) {
        return methodLevelAnnotations().stream()
                .anyMatch(a -> overridden.getAnnotation(a) != null);
    }
}
