package net.jbock.method;

import net.jbock.annotated.AnnotatedMethod;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public abstract class MethodAnnotation<M extends AnnotatedMethod> {

    private final M annotatedMethod;

    MethodAnnotation(M annotatedMethod) {
        this.annotatedMethod = annotatedMethod;
    }

    public final Optional<String> descriptionKey() {
        return annotatedMethod.descriptionKey();
    }

    public final Optional<String> label() {
        return annotatedMethod.label();
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

    public ExecutableElement sourceMethod() {
        return annotatedMethod.sourceMethod();
    }

    public M annotatedMethod() {
        return annotatedMethod;
    }
}
