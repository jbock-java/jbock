package net.jbock.parameter;

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
import java.util.stream.Collectors;

import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public class SourceMethod<M extends AnnotatedMethod> {

    private final MethodAnnotation<M> methodAnnotation;
    private final EnumName enumName;
    private final List<Modifier> accessModifiers;

    private SourceMethod(
            MethodAnnotation<M> methodAnnotation,
            EnumName enumName,
            List<Modifier> accessModifiers) {
        this.methodAnnotation = methodAnnotation;
        this.enumName = enumName;
        this.accessModifiers = accessModifiers;
    }

    public static SourceMethod<?> create(
            AnnotatedMethod annotatedMethod,
            EnumName enumName,
            int numberOfParameters) {
        List<Modifier> accessModifiers = annotatedMethod.sourceMethod().getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(Collectors.toUnmodifiableList());
        MethodAnnotation<?> annotation = annotatedMethod.annotation(numberOfParameters);
        return new SourceMethod<>(annotation, enumName, accessModifiers);
    }

    public ExecutableElement method() {
        return methodAnnotation.sourceMethod();
    }

    public TypeMirror returnType() {
        return methodAnnotation.sourceMethod().getReturnType();
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
        return new ValidationFailure(message, methodAnnotation.sourceMethod());
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
}
