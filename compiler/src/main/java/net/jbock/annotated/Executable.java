package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Constants.ACCESS_MODIFIERS;

abstract class Executable {

    private static final AnnotationUtil ANNOTATION_UTIL = new AnnotationUtil();

    private final ExecutableElement method;

    Executable(ExecutableElement method) {
        this.method = method;
    }

    static Executable create(ExecutableElement method, Annotation annotation) {
        if (annotation instanceof Option) {
            return new ExecutableOption(method, (Option) annotation);
        }
        if (annotation instanceof Parameter) {
            return new ExecutableParameter(method, (Parameter) annotation);
        }
        if (annotation instanceof Parameters) {
            return new ExecutableParameters(method, (Parameters) annotation);
        }
        throw new AssertionError("expecting one of " + methodLevelAnnotations()
                + " but found: " + annotation.getClass());
    }

    abstract AnnotatedMethod annotatedMethod(EnumName enumName);

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
        return ANNOTATION_UTIL.getConverterAttribute(method);
    }

    final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }
}
