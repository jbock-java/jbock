package net.jbock.method;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public abstract class MethodAnnotation {

    private final AnnotatedMethod annotatedMethod;

    MethodAnnotation(AnnotatedMethod annotatedMethod) {
        this.annotatedMethod = annotatedMethod;
    }

    public final Optional<String> descriptionKey() {
        return annotatedMethod.descriptionKey();
    }

    public final Optional<String> paramLabel() {
        return annotatedMethod.paramLabel();
    }

    public final boolean isPositional() {
        return annotatedMethod.isPositional();
    }

    public final boolean isParameters() {
        return annotatedMethod.isParameters();
    }

    public final boolean isParameter() {
        return annotatedMethod.isParameter();
    }

    public abstract OptionalInt index();

    public final List<String> names() {
        return annotatedMethod.names();
    }

    public final List<String> description() {
        return annotatedMethod.description();
    }

    public static MethodAnnotation create(
            AnnotatedMethod annotatedMethod,
            int numberOfParameters) {
        if (annotatedMethod instanceof AnnotatedOption) {
            return new OptionAnnotation((AnnotatedOption) annotatedMethod);
        }
        if (annotatedMethod instanceof AnnotatedParameter) {
            return new ParameterAnnotation(annotatedMethod, ((AnnotatedParameter) annotatedMethod).index());
        }
        if (annotatedMethod instanceof AnnotatedParameters) {
            return new ParameterAnnotation(annotatedMethod, numberOfParameters);
        }
        throw new AssertionError("all cases exhausted: " + annotatedMethod.getClass());
    }

    public ExecutableElement sourceMethod() {
        return annotatedMethod.sourceMethod();
    }
}
