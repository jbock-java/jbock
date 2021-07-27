package net.jbock.source;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public abstract class SourceMethod<M extends AnnotatedMethod> {

    private final EnumName enumName;

    SourceMethod(EnumName enumName) {
        this.enumName = enumName;
    }

    public ExecutableElement method() {
        return annotatedMethod().method();
    }

    public TypeMirror returnType() {
        return annotatedMethod().method().getReturnType();
    }

    public boolean isParameters() {
        return annotatedMethod().isParameters();
    }

    public boolean isParameter() {
        return annotatedMethod().isParameter();
    }

    public abstract OptionalInt index();

    public Optional<String> descriptionKey() {
        return annotatedMethod().descriptionKey();
    }

    public ValidationFailure fail(String message) {
        return new ValidationFailure(message, annotatedMethod().method());
    }

    public List<String> names() {
        return annotatedMethod().names();
    }

    public List<String> description() {
        return annotatedMethod().description();
    }

    public Optional<String> label() {
        return annotatedMethod().label();
    }

    public List<Modifier> accessModifiers() {
        return annotatedMethod().accessModifiers();
    }

    public EnumName enumName() {
        return enumName;
    }

    public abstract M annotatedMethod();

    public abstract Optional<SourceOption> asAnnotatedOption();

    public abstract Optional<SourceParameter> asAnnotatedParameter();

    public abstract Optional<SourceParameters> asAnnotatedParameters();
}
