package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

abstract class Executable {

    private static final Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);
    private static final AnnotationUtil ANNOTATION_UTIL = new AnnotationUtil();

    private final ExecutableElement method;
    private final AnnotationMirror annotationMirror;

    Executable(ExecutableElement method, AnnotationMirror annotationMirror) {
        this.method = method;
        this.annotationMirror = annotationMirror;
    }

    static Executable create(
            ExecutableElement method,
            Annotation annotation) {
        String canonicalName = findCanonicalName(annotation);
        AnnotationMirror annotationMirror = method.getAnnotationMirrors().stream()
                .filter(mirror -> AS_TYPE_ELEMENT.visit(mirror.getAnnotationType().asElement())
                        .map(TypeElement::getQualifiedName)
                        .map(Name::toString)
                        .filter(canonicalName::equals)
                        .isPresent())
                .findFirst()
                .orElseThrow(AssertionError::new);
        if (annotation instanceof Option) {
            return new ExecutableOption(method, (Option) annotation, annotationMirror);
        }
        if (annotation instanceof Parameter) {
            return new ExecutableParameter(method, (Parameter) annotation, annotationMirror);
        }
        if (annotation instanceof Parameters) {
            return new ExecutableParameters(method, (Parameters) annotation, annotationMirror);
        }
        throw new AssertionError();
    }

    private static String findCanonicalName(Annotation annotation) {
        if (annotation instanceof Option) {
            return Option.class.getCanonicalName();
        } else if (annotation instanceof Parameter) {
            return Parameter.class.getCanonicalName();
        } else if (annotation instanceof Parameters) {
            return Parameters.class.getCanonicalName();
        } else {
            throw new AssertionError();
        }
    }

    abstract AnnotatedMethod annotatedMethod(SourceElement sourceElement, String enumName);

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

    final Optional<TypeElement> converter() {
        return ANNOTATION_UTIL.getConverterAttribute(annotationMirror);
    }

    final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }
}
