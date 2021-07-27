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
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public abstract class AnnotatedMethod {

    private final ExecutableElement method;
    private final List<Modifier> accessModifiers;

    AnnotatedMethod(ExecutableElement method, List<Modifier> accessModifiers) {
        this.method = method;
        this.accessModifiers = accessModifiers;
    }

    public static AnnotatedMethod create(ExecutableElement sourceMethod, Annotation annotation) {
        List<Modifier> accessModifiers = sourceMethod.getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(toList());
        if (annotation instanceof Option) {
            return AnnotatedOption.create(sourceMethod, (Option) annotation, accessModifiers);
        }
        if (annotation instanceof Parameter) {
            return new AnnotatedParameter(sourceMethod, (Parameter) annotation, accessModifiers);
        }
        if (annotation instanceof Parameters) {
            return new AnnotatedParameters(sourceMethod, (Parameters) annotation, accessModifiers);
        }
        throw new AssertionError("expecting one of " +
                Annotations.methodLevelAnnotations() +
                " but found: " + annotation.getClass());
    }

    public abstract Optional<String> descriptionKey();

    public abstract Optional<String> label();

    public abstract boolean isParameter();

    public abstract boolean isParameters();

    public abstract List<String> description();

    public final ExecutableElement method() {
        return method;
    }

    public abstract SourceMethod<?> sourceMethod(EnumName enumName);

    public final List<Modifier> accessModifiers() {
        return accessModifiers;
    }

    public final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }
}
