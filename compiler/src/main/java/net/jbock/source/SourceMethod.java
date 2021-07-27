package net.jbock.source;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.method.MethodAnnotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.stream.Collectors.toList;
import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public abstract class SourceMethod<M extends AnnotatedMethod> {

    private final MethodAnnotation<M> methodAnnotation;
    private final EnumName enumName;
    private final List<Modifier> accessModifiers;

    SourceMethod(
            MethodAnnotation<M> methodAnnotation,
            EnumName enumName) {
        this.methodAnnotation = methodAnnotation;
        this.enumName = enumName;
        this.accessModifiers = methodAnnotation.method().getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(toList());
    }

    public ExecutableElement method() {
        return methodAnnotation.method();
    }

    public TypeMirror returnType() {
        return methodAnnotation.method().getReturnType();
    }

    public boolean isPositional() {
        return methodAnnotation.isPositional();
    }

    public boolean isParameters() {
        return methodAnnotation.isParameters();
    }

    public boolean isParameter() {
        return methodAnnotation.isParameter();
    }

    public OptionalInt index() {
        return methodAnnotation.index();
    }

    public Optional<String> descriptionKey() {
        return methodAnnotation.descriptionKey();
    }

    public ValidationFailure fail(String message) {
        return new ValidationFailure(message, methodAnnotation.method());
    }

    public List<String> names() {
        return methodAnnotation.names();
    }

    public List<String> description() {
        return methodAnnotation.description();
    }

    public Optional<String> label() {
        return methodAnnotation.label();
    }

    public List<Modifier> accessModifiers() {
        return accessModifiers;
    }

    public EnumName enumName() {
        return enumName;
    }

    public MethodAnnotation<M> methodAnnotation() {
        return methodAnnotation;
    }
}
