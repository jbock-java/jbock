package net.jbock.annotated;

import net.jbock.common.ValidationFailure;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.common.TypeTool.ANNOTATION_VALUE_AS_TYPE;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

public abstract class Item {

    private static final Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

    private final ExecutableElement method;
    private final Optional<TypeElement> converter;
    private final String enumName;

    Item(ExecutableElement method,
         Optional<TypeElement> converter,
         String enumName) {
        this.method = method;
        this.converter = converter;
        this.enumName = enumName;
    }

    static Item create(
            ExecutableElement method,
            Annotation annotation,
            String enumName) {
        String canonicalName = annotation.annotationType().getCanonicalName();
        AnnotationMirror annotationMirror = method.getAnnotationMirrors().stream()
                .filter(mirror -> AS_TYPE_ELEMENT.visit(mirror.getAnnotationType().asElement())
                        .map(TypeElement::getQualifiedName)
                        .map(Name::toString)
                        .filter(canonicalName::equals)
                        .isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        Optional<TypeElement> converter = findConverterAttribute(annotationMirror);
        if (annotation instanceof net.jbock.Option) {
            return new Option(method, (net.jbock.Option) annotation, converter, enumName);
        }
        if (annotation instanceof net.jbock.Parameter) {
            return new Parameter(method, (net.jbock.Parameter) annotation, converter, enumName);
        }
        if (annotation instanceof net.jbock.VarargsParameter) {
            return new VarargsParameter(method, (net.jbock.VarargsParameter) annotation, converter, enumName);
        }
        throw new AssertionError();
    }

    public abstract boolean isParameter();

    public abstract boolean isVarargsParameter();

    public abstract String paramLabel();

    public abstract Optional<String> descriptionKey();

    public abstract List<String> description();

    public final ExecutableElement method() {
        return method;
    }

    final Name simpleName() {
        return method.getSimpleName();
    }

    public final List<Modifier> accessModifiers() {
        return method().getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(toList());
    }

    private static Optional<TypeElement> findConverterAttribute(AnnotationMirror annotationMirror) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                annotationMirror.getElementValues();
        return elementValues.entrySet().stream()
                .filter(e -> "converter".contentEquals(e.getKey().getSimpleName()))
                .map(Map.Entry::getValue)
                .findFirst()
                .flatMap(ANNOTATION_VALUE_AS_TYPE::visit)
                .flatMap(AS_DECLARED::visit)
                .map(DeclaredType::asElement)
                .flatMap(AS_TYPE_ELEMENT::visit)
                .filter(element -> !"java.lang.Void".contentEquals(element.getQualifiedName()));
    }

    public final Optional<TypeElement> converter() {
        return converter;
    }

    public final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }

    public final String enumName() {
        return enumName;
    }

    public final String methodName() {
        return method.getSimpleName().toString();
    }

    public final TypeMirror returnType() {
        return method.getReturnType();
    }
}
