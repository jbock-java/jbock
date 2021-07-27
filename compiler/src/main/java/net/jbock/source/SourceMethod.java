package net.jbock.source;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public abstract class SourceMethod<M extends AnnotatedMethod> {

    private final EnumName enumName;

    SourceMethod(EnumName enumName) {
        this.enumName = enumName;
    }

    public final ExecutableElement method() {
        return annotatedMethod().method();
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

    public abstract M annotatedMethod();

    public abstract Optional<SourceOption> asAnnotatedOption();

    public abstract Optional<SourceParameter> asAnnotatedParameter();

    public abstract Optional<SourceParameters> asAnnotatedParameters();

    public abstract String paramLabel();

    public abstract boolean hasUnixName();
}
