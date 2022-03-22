package net.jbock.common;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static javax.lang.model.util.ElementFilter.constructorsIn;

public class Util {

    private final SafeTypes types;
    private final TypeTool tool;

    public Util(SafeTypes types, TypeTool tool) {
        this.types = types;
        this.tool = tool;
    }

    /* Left-Optional
     */
    public Optional<ValidationFailure> commonTypeChecks(TypeElement classToCheck) {
        return checkNesting(classToCheck)
                .map(f -> f.prepend("invalid class: "))
                .or(() -> checkDefaultConstructor(classToCheck));
    }

    private Optional<ValidationFailure> checkNesting(TypeElement classToCheck) {
        if (classToCheck.getNestingKind().isNested() && !classToCheck.getModifiers().contains(STATIC)) {
            return Optional.of(new ValidationFailure("nested class '" +
                    classToCheck.getSimpleName() +
                    "' must be static", classToCheck));
        }
        if (classToCheck.getModifiers().contains(PRIVATE)) {
            return Optional.of(new ValidationFailure("class '" +
                    classToCheck.getSimpleName() +
                    " may not be private", classToCheck));
        }
        for (TypeElement element : getEnclosingElements(classToCheck)) {
            if (element.getModifiers().contains(PRIVATE)) {
                return Optional.of(new ValidationFailure("enclosing class '" +
                        element.getSimpleName() +
                        "' may not be private", classToCheck));
            }
        }
        return Optional.empty();
    }

    private Optional<ValidationFailure> checkDefaultConstructor(TypeElement classToCheck) {
        List<ExecutableElement> constructors = constructorsIn(classToCheck.getEnclosedElements());
        if (constructors.isEmpty()) {
            return Optional.empty();
        }
        return constructors.stream()
                .filter(c -> c.getParameters().isEmpty())
                .map(c -> {
                    if (c.getModifiers().contains(PRIVATE)) {
                        return Optional.of(new ValidationFailure("invalid constructor:" +
                                " visibility may not be private", c));
                    }
                    return checkExceptionsInDeclaration(c);
                })
                .flatMap(Optional::stream)
                .findAny()
                .or(() -> {
                    if (constructors.stream().anyMatch(c -> c.getParameters().isEmpty())) {
                        return Optional.empty();
                    }
                    return Optional.of(new ValidationFailure("invalid converter class:" +
                            " default constructor not found", classToCheck));
                });
    }

    public Optional<ValidationFailure> checkExceptionsInDeclaration(ExecutableElement element) {
        return element.getThrownTypes().stream()
                .map(thrown -> checkChecked(element, thrown, thrown))
                .flatMap(Optional::stream)
                .findAny();
    }

    private Optional<ValidationFailure> checkChecked(
            ExecutableElement element,
            TypeMirror thrown,
            TypeMirror mirror) {
        if (tool.isSameType(mirror, RuntimeException.class) ||
                tool.isSameType(mirror, Error.class)) {
            return Optional.empty();
        }
        if (tool.isSameType(mirror, Throwable.class)) {
            return Optional.of(new ValidationFailure("invalid throws clause:" +
                    " found checked exception " +
                    typeToString(thrown), element));
        }
        return types.asElement(mirror)
                .flatMap(TypeTool.AS_TYPE_ELEMENT::visit)
                .flatMap(t -> checkNesting(t)
                        .map(f -> f.prepend("invalid throws clause: declared exception " +
                                typeToString(thrown) + " is invalid: ").about(element))
                        .or(() -> checkChecked(element, thrown, t.getSuperclass())));
    }

    public List<TypeElement> getEnclosingElements(TypeElement sourceElement) {
        LinkedList<TypeElement> result = new LinkedList<>();
        result.add(sourceElement);
        while (result.getLast().getNestingKind() == MEMBER) {
            Element enclosingElement = result.getLast().getEnclosingElement();
            TypeTool.AS_TYPE_ELEMENT.visit(enclosingElement)
                    .ifPresent(result::add);
        }
        return new ArrayList<>(result);
    }

    public static String typeToString(TypeMirror type) {
        return TypeTool.AS_DECLARED.visit(type).flatMap(declared ->
                TypeTool.AS_TYPE_ELEMENT.visit(declared.asElement()).map(t -> {
                    String base = t.getSimpleName().toString();
                    if (declared.getTypeArguments().isEmpty()) {
                        return base;
                    }
                    return base + declared.getTypeArguments().stream().map(Util::typeToString)
                            .collect(joining(", ", "<", ">"));
                })).orElseGet(type::toString);
    }

    /* Left-Optional
     */
    public static Optional<ValidationFailure> checkNoDuplicateAnnotations(
            ExecutableElement element,
            List<Class<? extends Annotation>> annotations) {
        List<Class<? extends Annotation>> present = annotations.stream()
                .filter(ann -> element.getAnnotation(ann) != null)
                .collect(toList());
        if (present.size() >= 2) {
            return Optional.of(new ValidationFailure("annotate with either @" + present.get(0).getSimpleName() +
                    " or @" + present.get(1).getSimpleName() + ", but not both", element));
        }
        return Optional.empty();
    }

    public SafeTypes types() {
        return types;
    }
}
