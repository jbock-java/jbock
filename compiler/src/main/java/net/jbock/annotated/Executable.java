package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.VarargsParameter;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
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

public abstract class Executable {

    private static final Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

    private final ExecutableElement method;
    private final Optional<TypeElement> converter;

    Executable(ExecutableElement method, Optional<TypeElement> converter) {
        this.method = method;
        this.converter = converter;
    }

    static Executable create(
            ExecutableElement method,
            Annotation annotation) {
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
        if (annotation instanceof Option) {
            return new ExecutableOption(method, (Option) annotation, converter);
        }
        if (annotation instanceof Parameter) {
            return new ExecutableParameter(method, (Parameter) annotation, converter);
        }
        if (annotation instanceof VarargsParameter) {
            return new ExecutableVarargsParameter(method, (VarargsParameter) annotation, converter);
        }
        throw new AssertionError();
    }

    abstract AnnotatedMethod<?> annotatedMethod(SourceElement sourceElement, String enumName);

    abstract Optional<String> descriptionKey();

    abstract List<String> description();

    final ExecutableElement method() {
        return method;
    }

    final Name simpleName() {
        return method.getSimpleName();
    }

    final List<Modifier> accessModifiers() {
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

    final Optional<TypeElement> converter() {
        return converter;
    }

    final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }
}
