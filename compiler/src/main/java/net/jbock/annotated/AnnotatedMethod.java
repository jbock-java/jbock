package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.common.Annotations;
import net.jbock.common.EnumName;
import net.jbock.method.MethodAnnotation;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public abstract class AnnotatedMethod {

    private final ExecutableElement sourceMethod;

    AnnotatedMethod(ExecutableElement sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    public static AnnotatedMethod create(ExecutableElement sourceMethod, Annotation annotation) {
        if (annotation instanceof Option) {
            return new AnnotatedOption(sourceMethod, (Option) annotation);
        }
        if (annotation instanceof Parameter) {
            return new AnnotatedParameter(sourceMethod, (Parameter) annotation);
        }
        if (annotation instanceof Parameters) {
            return new AnnotatedParameters(sourceMethod, (Parameters) annotation);
        }
        throw new AssertionError("expecting one of " +
                Annotations.methodLevelAnnotations() +
                " but found: " + annotation);
    }

    public abstract Optional<String> descriptionKey();

    public abstract Optional<String> label();

    public final boolean isPositional() {
        return isParameter() || isParameters();
    }

    public abstract boolean isParameters();

    public abstract boolean isParameter();

    public abstract List<String> names();

    public abstract List<String> description();

    public ExecutableElement sourceMethod() {
        return sourceMethod;
    }

    public abstract MethodAnnotation<?> annotation(int numberOfParameters);
}
