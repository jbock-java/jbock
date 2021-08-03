package net.jbock.annotated;

import io.jbock.util.Either;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<ExecutableElement> methods = findParameterlessMethodsIn(sourceElement.element().asType());
        Map<Boolean, List<ExecutableElement>> partitions = methods.stream()
                .collect(partitioningBy(m -> m.getModifiers().contains(ABSTRACT)));
        Set<Name> nonAbstractNames = partitions.get(false).stream()
                .map(ExecutableElement::getSimpleName)
                .collect(Collectors.toSet());
        List<ExecutableElement> allAbstract = partitions.get(true);
        Map<Name, List<ExecutableElement>> allAbstractByName = allAbstract.stream()
                .collect(groupingBy(ExecutableElement::getSimpleName, LinkedHashMap::new, toList()));
        return validateAbstractMethods(allAbstractByName, nonAbstractNames)
                .<Either<List<ValidationFailure>, List<Executable>>>map(Either::left)
                .orElseGet(() -> right(allAbstract.stream()
                        .flatMap(this::createExecutable)
                        .collect(toList())))
                .map(AnnotatedMethodsBuilder::builder)
                .map(step -> step.sourceElement(sourceElement));
    }

    private List<ExecutableElement> findParameterlessMethodsIn(TypeMirror mirror) {
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

    private Optional<List<ValidationFailure>> validateAbstractMethods(
            Map<Name, List<ExecutableElement>> allAbstractByName,
            Set<Name> nonAbstractNames) {
        Set<Name> names = allAbstractByName.keySet();
        return names.stream()
                .filter(name -> !nonAbstractNames.contains(name)
                        && missingAnnotation(allAbstractByName.get(name)))
                .map(m -> new ValidationFailure(missingAnnotationError(),
                        allAbstractByName.get(m).get(0)))
                .collect(toOptionalList())
                .or(() -> names.stream()
                        .filter(name -> nonAbstractNames.contains(name)
                                && allAbstractByName.get(name).stream().anyMatch(this::hasAnnotation)
                                || multiAnnotation(allAbstractByName.get(name)))
                        .map(m -> new ValidationFailure("annotated method is overridden",
                                allAbstractByName.get(m).get(0)))
                        .collect(toOptionalList()));
    }

    private boolean multiAnnotation(List<ExecutableElement> homonyms) {
        return homonyms.stream()
                .filter(this::hasAnnotation)
                .count() >= 2;
    }

    private boolean missingAnnotation(List<ExecutableElement> homonyms) {
        return homonyms.stream()
                .filter(this::hasAnnotation)
                .findAny()
                .isEmpty();
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

    private String missingAnnotationError() {
        return "add one of these annotations: " + methodLevelAnnotations().stream()
                .map(ann -> "@" + ann.getSimpleName())
                .collect(Collectors.joining(", "));
    }
}
