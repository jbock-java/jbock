package net.jbock.source;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.List;
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

    public final boolean isParameters() {
        return annotatedMethod().isParameters();
    }

    public final boolean isParameter() {
        return annotatedMethod().isParameter();
    }

    public final Optional<String> descriptionKey() {
        return annotatedMethod().descriptionKey();
    }

    public final ValidationFailure fail(String message) {
        return annotatedMethod().fail(message);
    }

    public final List<String> description() {
        return annotatedMethod().description();
    }

    public final Optional<String> label() {
        return annotatedMethod().label();
    }

    public final List<Modifier> accessModifiers() {
        return annotatedMethod().accessModifiers();
    }

    public final EnumName enumName() {
        return enumName;
    }

    public abstract M annotatedMethod();

    public abstract Optional<SourceOption> asAnnotatedOption();

    public abstract Optional<SourceParameter> asAnnotatedParameter();

    public abstract Optional<SourceParameters> asAnnotatedParameters();
}
