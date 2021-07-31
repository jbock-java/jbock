package net.jbock.common;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.util.StringConverter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

public class Util {

    private final Types types;
    private final TypeTool tool;

    public Util(Types types, TypeTool tool) {
        this.types = types;
        this.tool = tool;
    }

    /* Left-Optional
     */
    public Optional<String> commonTypeChecks(TypeElement classToCheck) {
        if (classToCheck.getNestingKind().isNested() && !classToCheck.getModifiers().contains(Modifier.STATIC)) {
            return Optional.of("nested class must be static");
        }
        for (TypeElement element : getEnclosingElements(classToCheck)) {
            if (element.getModifiers().contains(Modifier.PRIVATE)) {
                return Optional.of("class cannot be private");
            }
        }
        if (!hasDefaultConstructor(classToCheck)) {
            return Optional.of("default constructor not found");
        }
        return Optional.empty();
    }

    private boolean hasDefaultConstructor(TypeElement classToCheck) {
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(classToCheck.getEnclosedElements());
        if (constructors.isEmpty()) {
            return true;
        }
        for (ExecutableElement constructor : constructors) {
            if (!constructor.getModifiers().contains(Modifier.PRIVATE) &&
                    constructor.getParameters().isEmpty() &&
                    !throwsAnyCheckedExceptions(constructor)) {
                return true;
            }
        }
        return false;
    }

    public boolean throwsAnyCheckedExceptions(ExecutableElement element) {
        for (TypeMirror thrownType : element.getThrownTypes()) {
            if (!extendsRuntimeException(thrownType)) {
                return true;
            }
        }
        return false;
    }

    private boolean extendsRuntimeException(TypeMirror mirror) {
        if (mirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        if (tool.isSameType(mirror, RuntimeException.class)) {
            return true;
        }
        return TypeTool.AS_TYPE_ELEMENT.visit(types.asElement(mirror))
                .map(TypeElement::getSuperclass)
                .filter(this::extendsRuntimeException)
                .isPresent();
    }

    public List<TypeElement> getEnclosingElements(TypeElement sourceElement) {
        List<TypeElement> result = new ArrayList<>();
        TypeElement current = sourceElement;
        result.add(current);
        while (current.getNestingKind() == NestingKind.MEMBER) {
            Element enclosingElement = current.getEnclosingElement();
            if (enclosingElement.getKind() != ElementKind.CLASS) {
                return result;
            }
            Optional<TypeElement> opt = TypeTool.AS_TYPE_ELEMENT.visit(enclosingElement);
            if (opt.isEmpty()) {
                return result;
            }
            current = opt.orElseThrow();
            result.add(current);
        }
        return result;
    }

    public String typeToString(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return type.toString();
        }
        return TypeTool.AS_DECLARED.visit(type).flatMap(declared ->
                TypeTool.AS_TYPE_ELEMENT.visit(declared.asElement()).map(el -> {
                    String base = el.getSimpleName().toString();
                    if (declared.getTypeArguments().isEmpty()) {
                        return base;
                    }
                    return base + declared.getTypeArguments().stream().map(this::typeToString)
                            .collect(Collectors.joining(", ", "<", ">"));
                })).orElseGet(type::toString);
    }

    public Either<String, AnnotatedMethod> checkExactlyOneAnnotation(
            ExecutableElement element,
            List<Class<? extends Annotation>> annotations) {
        return checkAtLeastOneAnnotation(element, annotations)
                .filter(a -> checkNoDuplicateAnnotations(element, annotations))
                .map(a -> AnnotatedMethod.create(element, a));
    }

    private Either<String, Annotation> checkAtLeastOneAnnotation(
            Element element,
            List<Class<? extends Annotation>> annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            Annotation a = element.getAnnotation(annotation);
            if (a != null) {
                return right(a);
            }
        }
        return left("add one of these annotations: " + annotations.stream()
                .map(ann -> "@" + ann.getSimpleName())
                .collect(Collectors.joining(", ")));
    }

    /* Left-Optional
     */
    public Optional<String> checkNoDuplicateAnnotations(
            Element element,
            List<Class<? extends Annotation>> annotations) {
        List<Class<? extends Annotation>> present = annotations.stream()
                .filter(ann -> element.getAnnotation(ann) != null)
                .collect(Collectors.toUnmodifiableList());
        if (present.size() >= 2) {
            return Optional.of("annotate with either @" + present.get(0).getSimpleName() +
                    " or @" + present.get(1).getSimpleName() + " but not both");
        }
        return Optional.empty();
    }

    public String noMatchError(TypeMirror type) {
        return "define a converter that implements " +
                StringConverter.class.getSimpleName() +
                "<" + typeToString(type) + ">";
    }
}
