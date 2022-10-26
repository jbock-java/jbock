package net.jbock.annotated;

import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public abstract class AnnotatedMethod<E extends Executable> {

    private final E executable;

    AnnotatedMethod(E executable) {
        this.executable = executable;
    }

    final E executable() {
        return executable;
    }

    public final boolean isParameter() {
        return executable.isParameter();
    }

    public final boolean isVarargsParameter() {
        return executable.isVarargsParameter();
    }

    public final String paramLabel() {
        return executable.paramLabel();
    }

    public final String enumName() {
        return executable().enumName();
    }

    public final String methodName() {
        return method().getSimpleName().toString();
    }

    public final TypeMirror returnType() {
        return method().getReturnType();
    }

    public final List<Modifier> accessModifiers() {
        return executable.accessModifiers();
    }

    public final Optional<TypeElement> converter() {
        return executable.converter();
    }

    public final ValidationFailure fail(String message) {
        return executable.fail(message);
    }

    public final ExecutableElement method() {
        return executable.method();
    }

    public final Optional<String> descriptionKey() {
        return executable.descriptionKey();
    }

    public final List<String> description() {
        return executable.description();
    }
}
