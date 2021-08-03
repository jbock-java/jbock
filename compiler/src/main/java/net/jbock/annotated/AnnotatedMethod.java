package net.jbock.annotated;

import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public abstract class AnnotatedMethod {

    private final ExecutableElement method;
    private final List<Modifier> accessModifiers;
    private final Optional<TypeElement> converter;
    private final EnumName enumName;
    private final String paramLabel;

    AnnotatedMethod(
            ExecutableElement method,
            List<Modifier> accessModifiers,
            Optional<TypeElement> converter,
            EnumName enumName,
            String paramLabel) {
        this.method = method;
        this.accessModifiers = accessModifiers;
        this.converter = converter;
        this.enumName = enumName;
        this.paramLabel = paramLabel;
    }

    public final ExecutableElement method() {
        return method;
    }

    public final List<Modifier> accessModifiers() {
        return accessModifiers;
    }

    public final Optional<TypeElement> converter() {
        return converter;
    }

    public final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }

    public abstract Optional<String> descriptionKey();

    public abstract boolean isParameter();

    public abstract boolean isParameters();

    public abstract List<String> description();

    abstract Optional<AnnotatedOption> asAnnotatedOption();

    abstract Optional<AnnotatedParameter> asAnnotatedParameter();

    abstract Optional<AnnotatedParameters> asAnnotatedParameters();

    public final EnumName enumName() {
        return enumName;
    }

    public final String methodName() {
        return method.getSimpleName().toString();
    }

    public final TypeMirror returnType() {
        return method.getReturnType();
    }

    public final String paramLabel() {
        return paramLabel;
    }
}
