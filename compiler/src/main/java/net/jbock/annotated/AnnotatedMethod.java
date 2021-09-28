package net.jbock.annotated;

import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AnnotatedMethod {

    private final String enumName;
    private final String paramLabel;

    AnnotatedMethod(
            String enumName,
            String paramLabel) {
        this.enumName = enumName;
        this.paramLabel = paramLabel;
    }

    abstract Executable executable();

    public abstract boolean isParameter();

    public abstract boolean isParameters();

    abstract Stream<AnnotatedOption> asAnnotatedOption();

    abstract Stream<AnnotatedParameter> asAnnotatedParameter();

    abstract Stream<AnnotatedParameters> asAnnotatedParameters();

    public final String enumName() {
        return enumName;
    }

    public final String methodName() {
        return method().getSimpleName().toString();
    }

    public final TypeMirror returnType() {
        return method().getReturnType();
    }

    public final String paramLabel() {
        return paramLabel;
    }

    public final List<Modifier> accessModifiers() {
        return executable().accessModifiers();
    }

    public final Optional<TypeElement> converter() {
        return executable().converter();
    }

    public final ValidationFailure fail(String message) {
        return executable().fail(message);
    }

    public final ExecutableElement method() {
        return executable().method();
    }

    public final Optional<String> descriptionKey() {
        return executable().descriptionKey();
    }

    public final List<String> description() {
        return executable().description();
    }
}
