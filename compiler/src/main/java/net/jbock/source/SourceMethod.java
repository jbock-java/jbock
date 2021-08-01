package net.jbock.source;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

/**
 * Decorates an annotated method with an additional unique name (the enum name).
 *
 * @param <M> type of the annotated method
 */
public abstract class SourceMethod<M extends AnnotatedMethod> {

    private final EnumName enumName;
    private final M annotatedMethod;

    SourceMethod(M annotatedMethod, EnumName enumName) {
        this.enumName = enumName;
        this.annotatedMethod = annotatedMethod;
    }

    public final TypeMirror returnType() {
        return annotatedMethod().method().getReturnType();
    }

    public final Optional<String> descriptionKey() {
        return annotatedMethod().descriptionKey();
    }

    public final ValidationFailure fail(String message) {
        return annotatedMethod().fail(message);
    }

    public final EnumName enumName() {
        return enumName;
    }

    public final String methodName() {
        return annotatedMethod().method().getSimpleName().toString();
    }

    public final M annotatedMethod() {
        return annotatedMethod;
    }

    public abstract Optional<SourceOption> asAnnotatedOption();

    public abstract Optional<SourceParameter> asAnnotatedParameter();

    public abstract Optional<SourceParameters> asAnnotatedParameters();

    public abstract String paramLabel();
}
