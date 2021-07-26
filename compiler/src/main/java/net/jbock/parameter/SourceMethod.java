package net.jbock.parameter;

import net.jbock.common.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public class SourceMethod {

    private final AnnotatedMethod annotatedMethod;
    private final EnumName enumName;
    private final List<Modifier> accessModifiers;

    private SourceMethod(
            AnnotatedMethod annotatedMethod,
            EnumName enumName,
            List<Modifier> accessModifiers) {
        this.annotatedMethod = annotatedMethod;
        this.enumName = enumName;
        this.accessModifiers = accessModifiers;
    }

    public static SourceMethod create(AnnotatedMethod annotatedMethod, EnumName enumName) {
        List<Modifier> accessModifiers = annotatedMethod.sourceMethod().getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(Collectors.toUnmodifiableList());
        return new SourceMethod(annotatedMethod, enumName, accessModifiers);
    }

    public ExecutableElement method() {
        return annotatedMethod.sourceMethod();
    }

    public TypeMirror returnType() {
        return annotatedMethod.sourceMethod().getReturnType();
    }

    public boolean isPositional() {
        return annotatedMethod.annotation().isPositional();
    }

    public boolean isParameters() {
        return annotatedMethod.annotation().isParameters();
    }

    public boolean isParameter() {
        return annotatedMethod.annotation().isParameter();
    }

    public OptionalInt index() {
        return annotatedMethod.annotation().index();
    }

    public Optional<String> descriptionKey() {
        return annotatedMethod.annotation().descriptionKey();
    }

    public ValidationFailure fail(String message) {
        return new ValidationFailure(message, annotatedMethod.sourceMethod());
    }

    public List<String> names() {
        return annotatedMethod.annotation().names();
    }

    public List<String> description() {
        return annotatedMethod.annotation().description();
    }

    public Optional<String> paramLabel() {
        return annotatedMethod.annotation().paramLabel();
    }

    public List<Modifier> accessModifiers() {
        return accessModifiers;
    }

    public EnumName enumName() {
        return enumName;
    }
}
