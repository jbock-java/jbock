package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.common.Annotations;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.source.SourceMethod;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public abstract class AnnotatedMethod {

    private static final AnnotationUtil ANNOTATION_UTIL = new AnnotationUtil();

    private final ExecutableElement method;
    private final List<Modifier> accessModifiers;
    private final Optional<TypeElement> converter;

    AnnotatedMethod(
            ExecutableElement method,
            List<Modifier> accessModifiers,
            Optional<TypeElement> converter) {
        this.method = method;
        this.accessModifiers = accessModifiers;
        this.converter = converter;
    }

    public static AnnotatedMethod create(ExecutableElement sourceMethod, Annotation annotation) {
        Optional<TypeElement> converter = ANNOTATION_UTIL.getConverterAttribute(sourceMethod);
        List<Modifier> accessModifiers = sourceMethod.getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(toList());
        if (annotation instanceof Option) {
            return AnnotatedOption.create(
                    sourceMethod, converter, (Option) annotation, accessModifiers);
        }
        if (annotation instanceof Parameter) {
            return new AnnotatedParameter(sourceMethod, converter, (Parameter) annotation, accessModifiers);
        }
        if (annotation instanceof Parameters) {
            return new AnnotatedParameters(sourceMethod, converter, (Parameters) annotation, accessModifiers);
        }
        throw new AssertionError("expecting one of " +
                Annotations.methodLevelAnnotations() +
                " but found: " + annotation.getClass());
    }

    public final ExecutableElement method() {
        return method;
    }

    public final List<Modifier> accessModifiers() {
        return accessModifiers;
    }

    public final Optional<TypeElement> converter() {
        return converter;
    }

    public final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }

    public abstract Optional<String> descriptionKey();

    public abstract Optional<String> label();

    public abstract boolean isParameter();

    public abstract boolean isParameters();

    public abstract List<String> description();

    public abstract SourceMethod<?> sourceMethod(EnumName enumName);
}
